/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.cms.migration.web.internal.portlet.action.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.counter.kernel.service.CounterLocalService;
import com.liferay.portal.background.task.model.BackgroundTask;
import com.liferay.portal.background.task.service.BackgroundTaskLocalService;
import com.liferay.portal.kernel.backgroundtask.BackgroundTaskStatus;
import com.liferay.portal.kernel.backgroundtask.BackgroundTaskStatusRegistryUtil;
import com.liferay.portal.kernel.backgroundtask.constants.BackgroundTaskConstants;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCResourceCommand;
import com.liferay.portal.kernel.security.auth.PrincipalException;
import com.liferay.portal.kernel.security.permission.PermissionCheckerFactoryUtil;
import com.liferay.portal.kernel.service.CompanyLocalService;
import com.liferay.portal.kernel.test.portlet.MockLiferayResourceRequest;
import com.liferay.portal.kernel.test.portlet.MockLiferayResourceResponse;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.test.util.UserTestUtil;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.TimeZoneUtil;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;

import jakarta.portlet.PortletException;

import java.io.ByteArrayOutputStream;

import java.util.Date;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Átila Rodrigues
 */
@RunWith(Arquillian.class)
public class GetCMSMigrationsMVCResourceCommandTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new LiferayIntegrationTestRule();

	@Before
	public void setUp() throws Exception {
		_company = _companyLocalService.getCompany(
			TestPropsValues.getCompanyId());
	}

	@Test
	public void testServeResource() throws Exception {
		_inProgressBackgroundTask = _addBackgroundTask(
			_company.getCompanyId(), _company.getGroupId(),
			_CLASS_NAME_CMS_MIGRATION_BACKGROUND_TASK_EXECUTOR);

		_otherCompanyBackgroundTask = _addBackgroundTask(
			RandomTestUtil.randomLong(), RandomTestUtil.randomLong(),
			_CLASS_NAME_CMS_MIGRATION_BACKGROUND_TASK_EXECUTOR);
		_otherExecutorBackgroundTask = _addBackgroundTask(
			_company.getCompanyId(), _company.getGroupId(),
			RandomTestUtil.randomString());

		BackgroundTaskStatus backgroundTaskStatus =
			BackgroundTaskStatusRegistryUtil.registerBackgroundTaskStatus(
				_inProgressBackgroundTask.getBackgroundTaskId(), null);

		String itemTitle = RandomTestUtil.randomString();

		backgroundTaskStatus.setAttribute("counter", 4L);
		backgroundTaskStatus.setAttribute("itemTitle", itemTitle);
		backgroundTaskStatus.setAttribute("phase", "journal-articles");
		backgroundTaskStatus.setAttribute("total", 10L);

		try {
			JSONArray jsonArray = _jsonFactory.createJSONArray(
				_serveResource(TestPropsValues.getUser()));

			JSONObject jsonObject = _getMigrationJSONObject(
				_inProgressBackgroundTask, jsonArray);

			Assert.assertTrue(jsonObject.getBoolean("inProgress"));
			Assert.assertEquals(itemTitle, jsonObject.getString("itemTitle"));
			Assert.assertEquals(
				LanguageUtil.get(
					LocaleUtil.US, "cms-migration-journal-articles"),
				jsonObject.getString("phaseLabel"));
			Assert.assertEquals(40, jsonObject.getInt("progress"));

			Assert.assertNull(
				_getMigrationJSONObject(
					_otherCompanyBackgroundTask, jsonArray));
			Assert.assertNull(
				_getMigrationJSONObject(
					_otherExecutorBackgroundTask, jsonArray));
		}
		finally {
			BackgroundTaskStatusRegistryUtil.unregisterBackgroundTaskStatus(
				_inProgressBackgroundTask.getBackgroundTaskId());
		}

		_user = UserTestUtil.addUser();

		try {
			_serveResource(_user);

			Assert.fail();
		}
		catch (PortletException portletException) {
			Assert.assertTrue(
				portletException.getCause() instanceof
					PrincipalException.MustBeCompanyAdmin);
		}
	}

	private BackgroundTask _addBackgroundTask(
			long companyId, long groupId, String taskExecutorClassName)
		throws Exception {

		BackgroundTask backgroundTask =
			_backgroundTaskLocalService.createBackgroundTask(
				_counterLocalService.increment());

		backgroundTask.setGroupId(groupId);
		backgroundTask.setCompanyId(companyId);
		backgroundTask.setUserId(TestPropsValues.getUserId());
		backgroundTask.setCreateDate(new Date());
		backgroundTask.setName(RandomTestUtil.randomString());
		backgroundTask.setTaskExecutorClassName(taskExecutorClassName);
		backgroundTask.setCompleted(false);
		backgroundTask.setStatus(BackgroundTaskConstants.STATUS_IN_PROGRESS);

		return _backgroundTaskLocalService.updateBackgroundTask(backgroundTask);
	}

	private JSONObject _getMigrationJSONObject(
		BackgroundTask backgroundTask, JSONArray jsonArray) {

		for (int i = 0; i < jsonArray.length(); i++) {
			JSONObject jsonObject = jsonArray.getJSONObject(i);

			if (jsonObject.getLong("backgroundTaskId") ==
					backgroundTask.getBackgroundTaskId()) {

				return jsonObject;
			}
		}

		return null;
	}

	private String _serveResource(User user) throws Exception {
		MockLiferayResourceRequest mockLiferayResourceRequest =
			new MockLiferayResourceRequest();

		ThemeDisplay themeDisplay = new ThemeDisplay();

		themeDisplay.setCompany(_company);
		themeDisplay.setLocale(LocaleUtil.US);
		themeDisplay.setPermissionChecker(
			PermissionCheckerFactoryUtil.create(user));
		themeDisplay.setTimeZone(TimeZoneUtil.getDefault());

		mockLiferayResourceRequest.setAttribute(
			WebKeys.THEME_DISPLAY, themeDisplay);

		MockLiferayResourceResponse mockLiferayResourceResponse =
			new MockLiferayResourceResponse();

		_mvcResourceCommand.serveResource(
			mockLiferayResourceRequest, mockLiferayResourceResponse);

		ByteArrayOutputStream byteArrayOutputStream =
			(ByteArrayOutputStream)
				mockLiferayResourceResponse.getPortletOutputStream();

		return byteArrayOutputStream.toString();
	}

	private static final String
		_CLASS_NAME_CMS_MIGRATION_BACKGROUND_TASK_EXECUTOR =
			"com.liferay.cms.migration.web.internal.background.task." +
				"CMSMigrationBackgroundTaskExecutor";

	@Inject
	private BackgroundTaskLocalService _backgroundTaskLocalService;

	private Company _company;

	@Inject
	private CompanyLocalService _companyLocalService;

	@Inject
	private CounterLocalService _counterLocalService;

	@DeleteAfterTestRun
	private BackgroundTask _inProgressBackgroundTask;

	@Inject
	private JSONFactory _jsonFactory;

	@Inject(filter = "mvc.command.name=/cms_migration/get_cms_migrations")
	private MVCResourceCommand _mvcResourceCommand;

	@DeleteAfterTestRun
	private BackgroundTask _otherCompanyBackgroundTask;

	@DeleteAfterTestRun
	private BackgroundTask _otherExecutorBackgroundTask;

	@DeleteAfterTestRun
	private User _user;

}