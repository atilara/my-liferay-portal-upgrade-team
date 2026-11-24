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
	migrations: IMigration[];
	migrationsURL: string;
}

export default function CMSMigration({
	migrations: initialMigrations,
	migrationsURL,
	...configurationProps
}: IProps) {
	const {migrationIncomplete, migrations} = useCMSMigrations(
		migrationsURL,
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
