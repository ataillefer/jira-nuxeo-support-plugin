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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.common.Environment;
import org.nuxeo.connect.NuxeoConnectClient;
import org.nuxeo.connect.identity.LogicalInstanceIdentifier;
import org.nuxeo.connect.update.LocalPackage;
import org.nuxeo.connect.update.Package;
import org.nuxeo.connect.update.PackageState;
import org.nuxeo.connect.update.PackageType;
import org.nuxeo.connect.update.PackageVisibility;
import org.nuxeo.connect.update.standalone.StandaloneUpdateService;
import org.nuxeo.ecm.admin.SystemInfoManager;
import org.nuxeo.ecm.admin.runtime.RuntimeInstrospection;
import org.nuxeo.ecm.admin.runtime.SimplifiedBundleInfo;
import org.nuxeo.ecm.admin.runtime.SimplifiedServerInfo;
import org.nuxeo.ecm.server.info.service.ServerInfoCollector;
import org.nuxeo.launcher.config.ConfigurationGenerator;
import org.nuxeo.launcher.info.CommandInfo;
import org.nuxeo.launcher.info.CommandSetInfo;
import org.nuxeo.launcher.info.PackageInfo;

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
    public File collectInfoAsZip() throws Exception {

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

            // Installed marketplace packages
            ConfigurationGenerator cg = new ConfigurationGenerator();
            // String installedPackages = getInstalledPackages(cg.getEnv());
            // addZipEntry(out, "packages.info", installedPackages);

            // Nuxeo configuration (nuxeo.conf)
            File configFile = cg.getNuxeoConf();
            addZipEntry(out, "nuxeo.conf", configFile);

            // Server logs (server.log)
            File logFile = new File(cg.getLogDir(), "server.log");
            addZipEntry(out, "server.log", logFile);

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

    protected void addZipEntry(ZipOutputStream out, String entryPath,
            File entryFile) throws IOException {
        ZipEntry entry = new ZipEntry(entryPath);
        out.putNextEntry(entry);
        writeFileEntry(out, entryFile);
        out.closeEntry();
    }

    protected void writeFileEntry(OutputStream out, File entryFile)
            throws FileNotFoundException, IOException {
        FileInputStream fis = new FileInputStream(entryFile);
        byte[] bytes = new byte[1024];
        try {
            int read;
            while ((read = fis.read(bytes)) > 0) {
                out.write(bytes, 0, read);
            }
        } finally {
            fis.close();
        }
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

    protected String getInstalledPackages(Environment env) throws Exception {

        StandaloneUpdateService standaloneUpdateService = new StandaloneUpdateService(
                env);
        List<LocalPackage> packagesList = standaloneUpdateService.getPackages();

        if (packagesList.isEmpty()) {
            return "None";
        } else {
            NuxeoConnectClient.getPackageManager().sort(packagesList);
            StringBuilder sb = new StringBuilder();
            for (Package pkg : packagesList) {
                CommandSetInfo cset = new CommandSetInfo();
                CommandInfo cmdInfo = cset.newCommandInfo(CommandInfo.CMD_LIST);
                newPackageInfo(cmdInfo, pkg);
                String packageDescription;
                switch (pkg.getState()) {
                case PackageState.DOWNLOADING:
                    packageDescription = "downloading";
                    break;
                case PackageState.DOWNLOADED:
                    packageDescription = "downloaded";
                    break;
                case PackageState.INSTALLING:
                    packageDescription = "installing";
                    break;
                case PackageState.INSTALLED:
                    packageDescription = "installed";
                    break;
                case PackageState.STARTED:
                    packageDescription = "started";
                    break;
                case PackageState.REMOTE:
                    packageDescription = "remote";
                    break;
                default:
                    packageDescription = "unknown";
                    break;
                }
                packageDescription = String.format("%6s %11s\t", pkg.getType(),
                        packageDescription);
                if (pkg.getState() == PackageState.REMOTE
                        && pkg.getType() != PackageType.STUDIO
                        && pkg.getVisibility() != PackageVisibility.PUBLIC
                        && !LogicalInstanceIdentifier.isRegistered()) {
                    packageDescription += "Registration required for ";
                }
                packageDescription += String.format("%s (id: %s)\n",
                        pkg.getName(), pkg.getId());
                sb.append(packageDescription);
            }
            return sb.toString();
        }
    }

    protected PackageInfo newPackageInfo(CommandInfo cmdInfo, Package pkg) {
        PackageInfo packageInfo = new PackageInfo(pkg.getName(),
                pkg.getVersion().toString(), pkg.getId(), pkg.getState());
        cmdInfo.packages.add(packageInfo);
        return packageInfo;
    }

}
