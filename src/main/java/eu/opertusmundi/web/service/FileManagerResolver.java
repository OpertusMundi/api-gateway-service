package eu.opertusmundi.web.service;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import eu.opertusmundi.common.model.EnumOwningEntityType;

@Service
public class FileManagerResolver {

    private static final Logger logger = LoggerFactory.getLogger(FileManagerResolver.class);

    private final Map<EnumOwningEntityType, FileManager> registry = new HashMap<EnumOwningEntityType, FileManager>();

    @Autowired
    private ApplicationContext ctx;

    @PostConstruct
    private void registerFileManagers() {
        final Map<String, Object> beans = this.ctx.getBeansWithAnnotation(FileManagerType.class);

        beans.values().stream()
            .map(o -> (FileManager) o)
            .forEach(fm -> {
                final FileManagerType type = fm.getClass().getAnnotation(FileManagerType.class);
                if (type != null) {
                    this.registry.put(type.value(), fm);
                } else {
                    logger.error("Bean of type {} has not annotation of type {}", fm.getClass(), FileManagerType.class);
                }
            });
    }

    /**
     * Return a valid {@link FileManager} instance for the given owning entity
     * type
     *
     * @param type the type of the owning entity
     *
     * @return
     */
    public FileManager getFileManager(EnumOwningEntityType type) {
        return this.registry.get(type);
    }

}
