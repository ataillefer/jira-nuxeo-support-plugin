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
package org.nuxeo.ecm.server.info.service.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.admin.SystemInfoManager;
import org.nuxeo.ecm.admin.runtime.RuntimeInstrospection;
import org.nuxeo.ecm.admin.runtime.SimplifiedBundleInfo;
import org.nuxeo.ecm.admin.runtime.SimplifiedServerInfo;
import org.nuxeo.ecm.server.info.service.ServerInfoCollector;

/**
 * Default implementation of a {@link ServerInfoCollector}.
 *
 * @author Antoine Taillefer (ataillefer@nuxeo.com)
 * @since 5.7
 */
public class ServerInfoCollectorImpl implements ServerInfoCollector {

    private static final long serialVersionUID = -6234252904277239743L;

    private static final Log log = LogFactory.getLog(ServerInfoCollectorImpl.class);

    @Override
    public File collectInfoAsZip() throws IOException {

        // TODO: let the caller delete the file when done with it
        File tmpFile = File.createTempFile("NXserverInfo-", ".zip");
        tmpFile.deleteOnExit();

        FileOutputStream fos = new FileOutputStream(tmpFile);
        ZipOutputStream out = new ZipOutputStream(fos);
        out.setLevel(Deflater.DEFAULT_COMPRESSION);
        try {
            // System
            // TODO: externalize logic from Seam component to a service
            SystemInfoManager sysInfoManager = new SystemInfoManager();
            String sysInfo = sysInfoManager.getHostInfo();
            addZipEntry(out, "system.info", sysInfo);

            // Nuxeo distrib
            SimplifiedServerInfo serverInfo = RuntimeInstrospection.getInfo();
            String distribInfo = getDistribInfo(serverInfo);
            addZipEntry(out, "distrib.info", distribInfo);

            return tmpFile;
        } finally {
            try {
                out.close();
                fos.close();
            } catch (IOException e) {
                log.error("Error while closing output streams");
            }
        }
    }

    protected void addZipEntry(ZipOutputStream out, String entryPath,
            String entryStr) throws IOException {
        ZipEntry entry = new ZipEntry(entryPath);
        out.putNextEntry(entry);
        out.write(entryStr.getBytes());
        out.closeEntry();
    }

    protected String getDistribInfo(SimplifiedServerInfo serverInfo) {
        // TODO: use message bundles
        StringBuilder sb = new StringBuilder();
        appendInfoLine(sb, "Nuxeo application name",
                serverInfo.getApplicationName());
        appendInfoLine(sb, "Nuxeo application version",
                serverInfo.getApplicationVersion());
        appendInfoLine(sb, "Distribution name",
                serverInfo.getDistributionName());
        appendInfoLine(sb, "Distribution version",
                serverInfo.getDistributionVersion());
        appendInfoLine(sb, "Target application server",
                serverInfo.getDistributionHost());
        appendInfoLine(sb, "Distribution date",
                serverInfo.getDistributionDate());
        appendInfoList(sb, "Warnings on startup", serverInfo.getWarnings());
        appendBundleInfos(sb, "Deployed bundles", serverInfo.getBundleInfos());
        return sb.toString();
    }

    protected void appendInfoLine(StringBuilder sb, String label, String value) {
        sb.append(label);
        sb.append(": ");
        sb.append(value);
        sb.append("\n");
    }

    protected void appendInfoList(StringBuilder sb, String label,
            List<String> values) {
        sb.append(label);
        sb.append(":\n");
        int spacerLength = label.length() + 2;
        for (String value : values) {
            sb.append(StringUtils.leftPad("", spacerLength));
            sb.append(value);
            sb.append("\n");
        }
    }

    protected void appendBundleInfos(StringBuilder sb, String label,
            List<SimplifiedBundleInfo> values) {
        sb.append(label);
        sb.append(":\n");
        int spacerLength = label.length() + 2;
        for (SimplifiedBundleInfo value : values) {
            sb.append(StringUtils.leftPad("", spacerLength));
            sb.append(value.getName());
            sb.append("\t");
            sb.append(value.getVersion());
            sb.append("\n");
        }
    }

}
