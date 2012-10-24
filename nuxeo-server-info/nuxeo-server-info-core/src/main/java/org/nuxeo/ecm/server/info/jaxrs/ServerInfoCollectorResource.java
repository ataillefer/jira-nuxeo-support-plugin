/*
 * (C) Copyright 2012 Nuxeo SA (http://nuxeo.com/) and contributors.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * Contributors:
 *     Antoine Taillefer
 */
package org.nuxeo.ecm.server.info.jaxrs;

import java.io.File;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.nuxeo.ecm.server.info.service.ServerInfoCollector;
import org.nuxeo.ecm.webengine.model.WebObject;
import org.nuxeo.ecm.webengine.model.impl.ModuleRoot;
import org.nuxeo.runtime.api.Framework;

/**
 * Root entry for the WebEngine module that allows to collect server
 * information.
 *
 * @author Antoine Taillefer (ataillefer@nuxeo.com)
 * @since 5.7
 */
@Path("/collectServerInfo")
@WebObject(type = "collectServerInfoRoot")
public class ServerInfoCollectorResource extends ModuleRoot {

    protected static final String SERVER_INFO_ZIP_FILENAME = "server_info.zip";

    @GET
    @Path("info.zip")
    @Produces("application/zip")
    public Response doGet() throws Exception {

        ServerInfoCollector serverInfoCollector = Framework.getLocalService(ServerInfoCollector.class);
        // TODO: find a way to delete the File after written in the response by
        // JAX-RS
        File serverInfoZip = serverInfoCollector.collectInfoAsZip();
        ResponseBuilder responseBuilder = Response.ok(serverInfoZip);
        responseBuilder.type(MediaType.APPLICATION_OCTET_STREAM);
        responseBuilder.header("Content-Disposition", String.format(
                "attachment; filename=\"%s\"", SERVER_INFO_ZIP_FILENAME));
        return responseBuilder.build();
    }
}
