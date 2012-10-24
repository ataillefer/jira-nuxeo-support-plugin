# JIRA plugin for Nuxeo

Plugin for JIRA to attach to an issue a ZIP containing useful information fetched from a Nuxeo server, such as:

- system information (OS, JVM)
- Nuxeo distribution
- installed marketplace packages
- Nuxeo configuration (nuxeo.conf)
- server logs (server.log)

## Principle

### Nuxeo side

The ``nuxeo-server-info`` plugin declares:

- a service to collect the information: ``ServerInfoCollector``
- a JAX-RS resource to expose this service by allowing to download a ZIP file containing the collected information

The JAX-RS resource URL is:

    http://server:port/nuxeo/site/collectServerInfo/info.zip

The ZIP structure is:

    server_info.zip
      |__ system.info
      |__ distrib.info
      |__ packages.info
      |__ nuxeo.conf
      |__ server.log

### JIRA side

The plugin adds a new action to the ``More Actions`` menu that opens a popup with a form asking for the Nuxeo server URL and the user credentials.
Submitting the form collects the information from the Nuxeo server as a ZIP file and attaches it to the issue.
