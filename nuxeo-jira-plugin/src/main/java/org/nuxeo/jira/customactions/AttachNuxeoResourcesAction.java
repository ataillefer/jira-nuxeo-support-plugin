package org.nuxeo.jira.customactions;

import java.util.ArrayList;
import java.util.Collection;

import org.nuxeo.jira.NuxeoServerInfoCollector;
import org.ofbiz.core.util.UtilDateTime;

import com.atlassian.jira.bc.issue.attachment.AttachmentService;
import com.atlassian.jira.bc.issue.comment.CommentService;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.event.issue.IssueEventSource;
import com.atlassian.jira.event.type.EventType;
import com.atlassian.jira.issue.AttachmentManager;
import com.atlassian.jira.issue.TemporaryAttachmentsMonitorLocator;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.screen.FieldScreenRendererFactory;
import com.atlassian.jira.issue.history.ChangeItemBean;
import com.atlassian.jira.issue.util.IssueUpdateBean;
import com.atlassian.jira.issue.util.IssueUpdater;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.web.action.issue.AttachFile;

public class AttachNuxeoResourcesAction extends AttachFile {

    public AttachNuxeoResourcesAction(
            SubTaskManager subTaskManager,
            FieldScreenRendererFactory fieldScreenRendererFactory,
            FieldManager fieldManager,
            ProjectRoleManager projectRoleManager,
            CommentService commentService,
            AttachmentService attachmentService,
            AttachmentManager attachmentManager,
            IssueUpdater issueUpdater,
            TemporaryAttachmentsMonitorLocator temporaryAttachmentsMonitorLocator,
            UserUtil userUtil) {
        super(subTaskManager, fieldScreenRendererFactory, fieldManager,
                projectRoleManager, commentService, attachmentService,
                attachmentManager, issueUpdater,
                temporaryAttachmentsMonitorLocator, userUtil);
        this.attachmentManager = attachmentManager;
        this.issueUpdater = issueUpdater;

    }

    private static final long serialVersionUID = 1L;

    protected String nxserver_passwd;

    protected String nxserver_user;

    protected String nxserver_url;

    private Long issueId;

    private AttachmentManager attachmentManager;

    private IssueUpdater issueUpdater;

    @Override
    public String doDefault() throws Exception {
        issueId = (Long) getIssue().get("id");
        return super.doDefault();
    }

    @Override
    protected void doValidation() {
        // TODO: validate url
    }

    @SuppressWarnings("deprecation")
    @Override
    protected String doExecute() throws Exception {
        final Collection<ChangeItemBean> changeItemBeans = new ArrayList<ChangeItemBean>();
        changeItemBeans.add(attachmentManager.createAttachment(
                NuxeoServerInfoCollector.collectNuxeoServerInfoAsZip(
                        getNxserver_url(), getNxserver_user(),
                        getNxserver_passwd()), "nx-server-info.zip",
                "application/zip", getLoggedInUser(), getIssue(), null,
                UtilDateTime.nowTimestamp()));

        final IssueUpdateBean issueUpdateBean = new IssueUpdateBean(getIssue(),
                getIssue(), EventType.ISSUE_UPDATED_ID, getLoggedInUser());
        issueUpdateBean.setComment(createComment());
        issueUpdateBean.setChangeItems(changeItemBeans);
        issueUpdateBean.setDispatchEvent(true);
        issueUpdateBean.setParams(MapBuilder.singletonMap("eventsource",
                IssueEventSource.ACTION));
        issueUpdater.doUpdate(issueUpdateBean, true);
        return returnComplete();
    }

    public String getNxserver_passwd() {
        return nxserver_passwd;
    }

    public void setNxserver_passwd(String nxserver_passwd) {
        this.nxserver_passwd = nxserver_passwd;
    }

    public String getNxserver_user() {
        return nxserver_user;
    }

    public void setNxserver_user(String nxserver_user) {
        this.nxserver_user = nxserver_user;
    }

    public String getNxserver_url() {
        return nxserver_url;
    }

    public void setNxserver_url(String nxserver_url) {
        this.nxserver_url = nxserver_url;
    }

    public Long getIssueId() {
        return issueId;
    }

}