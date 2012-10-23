# JIRA plugin for Nuxeo

Automatically attach to a JIRA issue a zip containing useful information coming from the server:
- the server log
- server configuration
- installed packages
- nuxeo.conf
- XML export

# Principle

## Nuxeo side

- expose un service that allows to downlaod the zip (basic auth)

## JIRA side

- create a plugin with a form including: server URL, userName, password# JIRA plugin for Nuxeo

