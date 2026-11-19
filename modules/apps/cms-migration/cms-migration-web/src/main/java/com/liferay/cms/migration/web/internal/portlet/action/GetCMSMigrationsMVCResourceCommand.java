/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.cms.migration.web.internal.portlet.action;

import com.liferay.cms.migration.web.internal.constants.CMSMigrationPortletKeys;
import com.liferay.cms.migration.web.internal.helper.CMSMigrationJSONHelper;
import com.liferay.portal.kernel.backgroundtask.BackgroundTaskManager;
import com.liferay.portal.kernel.portlet.JSONPortletResponseUtil;
import com.liferay.portal.kernel.portlet.bridges.mvc.BaseMVCResourceCommand;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCResourceCommand;
import com.liferay.portal.kernel.security.auth.PrincipalException;
import com.liferay.portal.kernel.security.permission.PermissionChecker;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.WebKeys;

import jakarta.portlet.ResourceRequest;
import jakarta.portlet.ResourceResponse;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Returns the migrations of the company, so the CMS Migration page updates
 * their progress without reloading.
 *
 * @author Átila Rodrigues
 */
@Component(
	property = {
		"jakarta.portlet.name=" + CMSMigrationPortletKeys.CMS_MIGRATION,
		"mvc.command.name=/cms_migration/get_cms_migrations"
	},
	service = MVCResourceCommand.class
)
public class GetCMSMigrationsMVCResourceCommand extends BaseMVCResourceCommand {

	@Override
	protected void doServeResource(
			ResourceRequest resourceRequest, ResourceResponse resourceResponse)
		throws Exception {

		ThemeDisplay themeDisplay = (ThemeDisplay)resourceRequest.getAttribute(
			WebKeys.THEME_DISPLAY);

		PermissionChecker permissionChecker =
			themeDisplay.getPermissionChecker();

		if (!permissionChecker.isCompanyAdmin()) {
			throw new PrincipalException.MustBeCompanyAdmin(
				permissionChecker.getUserId());
		}

		CMSMigrationJSONHelper cmsMigrationJSONHelper =
			new CMSMigrationJSONHelper(
				_backgroundTaskManager,
				_portal.getLiferayPortletResponse(resourceResponse),
				themeDisplay);

		JSONPortletResponseUtil.writeJSON(
			resourceRequest, resourceResponse,
			cmsMigrationJSONHelper.getMigrationsJSONArray());
	}

	@Reference
	private BackgroundTaskManager _backgroundTaskManager;

	@Reference
	private Portal _portal;

}