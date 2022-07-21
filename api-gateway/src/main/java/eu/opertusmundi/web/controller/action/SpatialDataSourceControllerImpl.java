package eu.opertusmundi.web.controller.action;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.RestController;

import eu.opertusmundi.common.domain.EncodingEntity;
import eu.opertusmundi.common.domain.EpsgEntity;
import eu.opertusmundi.common.domain.NutsRegionEntity;
import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.common.model.spatial.EpsgDto;
import eu.opertusmundi.common.model.spatial.FeatureCollectionDto;
import eu.opertusmundi.common.model.spatial.NutsRegionFeatureDto;
import eu.opertusmundi.common.model.spatial.NutsRegionPropertiesDto;
import eu.opertusmundi.common.repository.EncodingRepository;
import eu.opertusmundi.common.repository.EpsgRepository;
import eu.opertusmundi.common.repository.NutsRegionRepository;

@RestController
public class SpatialDataSourceControllerImpl extends BaseController implements SpatialDataSourceController, InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(SpatialDataSourceControllerImpl.class);

    private final String PARAMETER_BBOX = "bbox";

    private final String CONTENT_TYPE_HEADER = "Content-Type";

    @Value("${opertusmundi.spatial.nuts.schema:spatial}")
    private String schema;

    @Value("${opertusmundi.spatial.nuts.table-name:nuts}")
    private String tableName;

    @Value("${opertusmundi.spatial.nuts.id-column:gid}")
    private String idColumn;

    @Value("${opertusmundi.spatial.nuts.geometry-column:geom}")
    private String geometryColumn;

    @Value("${opertusmundi.spatial.nuts.geometry-column-simple:geom_simple}")
    private String geometryColumnSimple;

    private final Map<String, List<String>> tableColumns = new HashMap<String, List<String>>();

    @Autowired
    private DataSource dataSource;

    private JdbcTemplate jdbcTemplate;

    @Autowired
    private EpsgRepository epsgRepository;

    @Autowired
    private EncodingRepository encodingRepository;

    @Autowired
    private NutsRegionRepository nutsRegionRepository;

    @Override
    public void afterPropertiesSet() throws Exception {
        jdbcTemplate = new JdbcTemplate(dataSource);

        // Custom mappings for spatial.nuts table
        tableColumns.put("nuts", Arrays.asList(
            "nuts_id as \"code\"",
            "lvl_code as \"level\"",
            "name_latin as \"nameLatin\"",
            "nuts_name as \"name\"",
            "population"
        ) );
    }

    @Override
    public RestResponse<?> findAllEpsg(String name, String code) {
        final List<EpsgDto> result = this.epsgRepository.findAllActive(toLikeClause(name), toLikeClause(code)).stream()
            .map(EpsgEntity::toDto)
            .collect(Collectors.toList());

        return RestResponse.result(result);
    }

    @Override
    public RestResponse<?> findAllEncoding(String code) {
        final List<String> result = this.encodingRepository.findAllActive(toLikeClause(code)).stream()
            .map(EncodingEntity::getCode)
            .collect(Collectors.toList());

        return RestResponse.result(result);
    }

    @Override
    public RestResponse<?> findAllByCode(String codes[]) {
        if (codes == null || codes.length == 0) {
            return RestResponse.success();
        }

        final List<NutsRegionFeatureDto> features = nutsRegionRepository.findByCode(codes).stream()
            .map(NutsRegionEntity::toFeature)
            .collect(Collectors.toList());

        return RestResponse.result(FeatureCollectionDto.of(features));
    }

    @Override
    public RestResponse<?> findOneByCode(String code) {
        if (StringUtils.isBlank(code)) {
            return RestResponse.success();
        }

        final NutsRegionFeatureDto feature = nutsRegionRepository.findByCode(code).map(NutsRegionEntity::toFeature).orElse(null);

        return RestResponse.result(feature);
    }

    @Override
    public RestResponse<?> findAllByName(Long level, String query) {
        if (StringUtils.isBlank(query) || query.length() < 4) {
            return RestResponse.result(new ArrayList<NutsRegionPropertiesDto>());
        }

        final List<NutsRegionPropertiesDto> result = this.nutsRegionRepository.findAllByNameContainsAndLevel(query, level).stream()
            .map(NutsRegionEntity::toProperties)
            .collect(Collectors.toList());

        return RestResponse.result(result);
    }

    @Override
    public RestResponse<?> findAllByPrefix(String prefix, Long maxLevel) {
        if (StringUtils.isBlank(prefix) || prefix.length() < 2) {
            return RestResponse.result(new ArrayList<NutsRegionPropertiesDto>());
        }

        if (maxLevel != null && maxLevel < 0) {
            maxLevel = null;
        }

        final List<NutsRegionFeatureDto> features = this.nutsRegionRepository.findAllByCodeStartsWith(prefix, prefix, maxLevel).stream()
            .map(NutsRegionEntity::toFeature)
            .collect(Collectors.toList());

        return RestResponse.result(FeatureCollectionDto.of(features));
    }

    @Override
    public void wfs(Integer level, String bbox, boolean includeGeometry, HttpServletRequest request, HttpServletResponse response) {
        try {
            loadFeatures(
                request, response,
                this.schema, this.tableName,
                this.idColumn, this.geometryColumn, this.geometryColumnSimple,
                level, includeGeometry
            );
        } catch (final Exception ex) {
            logger.error(String.format("NUTS regions WFS request has failed. [message=%s]:" + ex.getMessage()), ex);

            response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private void loadFeatures(
        HttpServletRequest request, HttpServletResponse response,
        String schema, String tableName,
        String idColumn, String geometryColumn, String geometryColumnSimple,
        int level, boolean includeGeometry
    ) throws IOException {
        final Map<String, String[]> parameterMap = request.getParameterMap();

        // Get bounding box
        final String boundingBox = parameterMap.keySet().stream()
            .filter(p -> p.equalsIgnoreCase(PARAMETER_BBOX))
            .map(p -> parameterMap.get(p)[0])
            .findFirst()
            .orElse(null);

        // Get filters
        if (boundingBox != null) {
            String[] boundingBoxParts = StringUtils.split(boundingBox, ",");
            // Ignore CRS from bounding box
            if (boundingBoxParts.length == 5) {
                boundingBoxParts = ArrayUtils.remove(boundingBoxParts, 4);
            }

            // Get table schema
            final List<String> columns = getColumns(tableName, geometryColumn, geometryColumnSimple);

            // Create where clause
            final String where = "ST_Intersects(ST_MakeEnvelope(?, ?, ?, ?, 4326), \"%6$s\") = true and lvl_code = ? ";

            // Get data as GeoJson
            String dataQuery =
                  "select row_to_json(fc) "
                + "from   ( "
                + "    select 'FeatureCollection' As type, COALESCE(array_to_json(array_agg(f)), '[]') As features "
                + "    from   ("
                + "               select 'Feature' As type, ";


            if(includeGeometry) {
                dataQuery += "            COALESCE(ST_AsGeoJSON(dt.\"%7$s\")::json, ST_AsGeoJSON(dt.\"%6$s\")::json) As geometry,";
            }

            dataQuery +=
                  "                      row_to_json((select columns FROM (SELECT %3$s) As columns)) As properties, "
                + "                      '%2$s::' || dt.\"%5$s\" as id "
                + "               from   \"%1$s\".\"%2$s\" As dt"
                + "               where  " + where
                + "    ) As f "
                + ")  As fc";

            dataQuery = String.format(
                dataQuery, schema, tableName, String.join(",", columns), boundingBox, idColumn, geometryColumn, geometryColumnSimple
            );

            final Object[] args =  new Object[] {
                Float.parseFloat(boundingBoxParts[0]),
                Float.parseFloat(boundingBoxParts[1]),
                Float.parseFloat(boundingBoxParts[2]),
                Float.parseFloat(boundingBoxParts[3]),
                level
            };

            final String output = jdbcTemplate.queryForObject(dataQuery, String.class, args);

            response.addHeader(CONTENT_TYPE_HEADER, "application/json; charset=UTF-8");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(output);
        }

        response.setStatus(HttpServletResponse.SC_OK);
    }

    private List<String> getColumns(String tableName, String geometryColumn, String geometryColumnSimple) {
        if (tableColumns.containsKey(tableName)) {
            return tableColumns.get(tableName);
        }
        synchronized (tableColumns) {
            if (tableColumns.containsKey(tableName)) {
                return tableColumns.get(tableName);
            }

            final String columnQuery = String.format("select column_name from information_schema.columns where table_name = '%s'", tableName);

            final List<Map<String, Object>> rows    = jdbcTemplate.queryForList(columnQuery);
            final List<String>              columns = rows.stream()
                .map(r -> (String) r.get("column_name"))
                .filter(c -> !c.equalsIgnoreCase(geometryColumn) && !c.equalsIgnoreCase(geometryColumnSimple))
                .collect(Collectors.toList());

            tableColumns.put(tableName, columns);
            return columns;

        }
    }

    private String toLikeClause(String value) {
        if (StringUtils.isBlank(value)) {
            return null;
        } else {
            String result = value;
            if (!result.startsWith("%")) {
                result = "%" + result;
            }
            if (!result.endsWith("%")) {
                result += "%";
            }
            return result;
        }
    }
}
