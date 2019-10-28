package com.zemiak.nasphotos.boundary;

import javax.inject.Inject;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.zemiak.nasphotos.SafeFile;
import com.zemiak.nasphotos.control.FolderControl;
import com.zemiak.nasphotos.control.PictureControl;

import org.eclipse.microprofile.config.inject.ConfigProperty;

@Produces(MediaType.APPLICATION_JSON)
@Path("backend/browse")
public class FilesResource {
    @Inject FolderControl folders;
    @Inject PictureControl pictures;

    @Inject @ConfigProperty(name = "photoPath") String photoPath;

    @GET
    public Response download(@QueryParam("path") @DefaultValue("/") String path) {
        if (! SafeFile.isSafe(path)) {
            return Response.status(Response.Status.FORBIDDEN).entity("Path " + path + " is unsafe").build();
        }

        if (folders.isLeafFolder(path)) {
            return Response.ok(pictures.getPictures(path)).build();
        }

        return Response.ok(folders.getList(path)).build();
    }
}
