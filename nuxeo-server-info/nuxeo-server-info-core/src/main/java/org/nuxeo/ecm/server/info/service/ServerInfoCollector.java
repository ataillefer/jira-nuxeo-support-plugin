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
package org.nuxeo.ecm.server.info.service;

import java.io.File;
import java.io.Serializable;

/**
 * Allows to collect server information such as:
 * <ul>
 * <li>system information (OS, JVM)</li>
 * <li>Nuxeo distribution</li>
 * <li>installed marketplace packages</li>
 * <li>Nuxeo configuration (nuxeo.conf)</li>
 * <li>server logs (server.log)</li>
 * </ul>
 *
 * @author Antoine Taillefer (ataillefer@nuxeo.com)
 * @since 5.7
 */
public interface ServerInfoCollector extends Serializable {

    /**
     * Collects server info as a ZIP file.
     */
    File collectInfoAsZip() throws Exception;

}
