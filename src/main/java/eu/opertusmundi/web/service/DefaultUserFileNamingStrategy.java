package eu.opertusmundi.web.service;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

@Service
public class DefaultUserFileNamingStrategy extends AbstractUserFileNamingStrategy {

    public static final String SCHEME = "user-data";

    @Autowired
    private Path userDataDirectory;

    @Override
    public String getScheme() {
        return SCHEME;
    }

    @Override
    public Path getUserDir(int userId) {
        Assert.isTrue(userId > 0, "Expected a valid (> 0) user id");

        return this.userDataDirectory.resolve(Integer.toString(userId));
    }

    @Override
    public URI convertToUri(int userId, Path relativePath) {
        Assert.isTrue(userId > 0, "Expected a valid (>0) user id");
        Assert.notNull(relativePath, "A path is required");
        Assert.isTrue(!relativePath.isAbsolute(), "Expected a relative path");

        URI uri = null;
        try {
            uri = new URI(SCHEME, String.valueOf(userId), "/" + relativePath.toString(), null);
        } catch (final URISyntaxException ex) {
            throw new IllegalArgumentException(ex);
        }
        return uri;
    }

    @Override
    public URI convertToUri(Path path) {
        Assert.notNull(path, "A path is required");
        Assert.isTrue(path.isAbsolute(), "Expected an absolute path");
        Assert.isTrue(path.startsWith(this.userDataDirectory), "The path is outside user-data directory");

        final Path userPath       = this.userDataDirectory.relativize(path);
        final int  userPathLength = userPath.getNameCount();

        Assert.isTrue(userPathLength > 1, "The relative path is too short");

        final int userId = Integer.parseInt(userPath.getName(0).toString());

        return this.convertToUri(userId, userPath.subpath(1, userPathLength));
    }

    @Override
    public Path resolveUri(URI uri) {
        Assert.notNull(uri, "A user-data URI is required");
        Assert.isTrue(SCHEME.equals(uri.getScheme()), "The given URI has an unexpected scheme");

        final int    userId = Integer.parseInt(uri.getHost());
        final String path   = uri.getPath();

        Assert.isTrue(path.startsWith("/"), "The URI path was expected as an absolute path");

        return this.resolvePath(userId, Paths.get(path.substring(1)));
    }

}