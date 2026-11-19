/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import React from 'react';

import useCMSMigrations from '../hooks/useCMSMigrations';
import Configuration from './Configuration';
import Migrations, {IMigration} from './Migrations';

interface IProps
	extends Omit<
		React.ComponentProps<typeof Configuration>,
		'migrationIncomplete'
	> {
	getCMSMigrationsURL: string;
	migrations: IMigration[];
}

export default function CMSMigration({
	getCMSMigrationsURL,
	migrations: initialMigrations,
	...configurationProps
}: IProps) {
	const {migrationIncomplete, migrations} = useCMSMigrations(
		getCMSMigrationsURL,
		initialMigrations
	);

	return (
		<>
			<Configuration
				{...configurationProps}
				migrationIncomplete={migrationIncomplete}
			/>

			<Migrations
				migrationIncomplete={migrationIncomplete}
				migrations={migrations}
			/>
		</>
	);
}
