/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import ClayIcon from '@clayui/icon';
import ClayProgressBar from '@clayui/progress-bar';
import classNames from 'classnames';
import React from 'react';

interface IMigrationPhase {
	completed: number;
	current: boolean;
	key: string;
	pending: boolean;
	total: number;
}

export interface IMigrationBar {
	completed: number;
	current: boolean;
	key: string;
	label: string;
	pending: boolean;
	phases: IMigrationPhase[];
	progress: number;
	total: number;
}

export default function MigrationBar({
	bar,
	numberFormat,
}: {
	bar: IMigrationBar;
	numberFormat: Intl.NumberFormat;
}) {
	const {completed, current, label, pending, phases, progress, total} = bar;

	const complete = progress === 100;
	const highlighted = current && !complete;
	const notStarted = !complete && !current && !completed;

	return (
		<>
			<div
				className={classNames({
					'font-weight-semi-bold text-info': highlighted,
					'text-secondary': notStarted,
					'text-success': complete,
				})}
			>
				{complete && <ClayIcon className="mr-2" symbol="check" />}

				{label}
			</div>

			<div
				className="text-right text-secondary"
				title={
					pending
						? Liferay.Language.get('cms-migration-total-pending')
						: undefined
				}
			>
				{`${numberFormat.format(completed)} / ${numberFormat.format(total)}${pending ? '+' : ''}`}
			</div>

			<ClayProgressBar
				aria-label={label}
				fillBarClassName="rounded-pill"
				value={progress}
			>
				{`${progress}%`}
			</ClayProgressBar>

			{!!phases.length && (
				<ul className="cms-migration-phases list-inline mb-0 small text-secondary">
					{phases.map((phase) => (
						<li
							className={classNames('list-inline-item', {
								'font-weight-semi-bold text-info':
									phase.current,
							})}
							key={phase.key}
							title={
								phase.pending
									? Liferay.Language.get(
											'cms-migration-total-pending'
										)
									: undefined
							}
						>
							{`${getPhaseLabel(phase.key)} ${numberFormat.format(phase.completed)} / ${phase.pending ? '…' : numberFormat.format(phase.total)}`}
						</li>
					))}
				</ul>
			)}
		</>
	);
}

function getPhaseLabel(key: string) {
	const labels: {[key: string]: string} = {
		'blogs-entries': Liferay.Language.get('cms-migration-blogs-entries'),
		'collections': Liferay.Language.get('collections'),
		'content-links': Liferay.Language.get('cms-migration-content-links'),
		'display-page-templates': Liferay.Language.get(
			'cms-migration-display-page-templates'
		),
		'file-entries': Liferay.Language.get('cms-migration-file-entries'),
		'folders': Liferay.Language.get('cms-migration-folders'),
		'journal-articles': Liferay.Language.get(
			'cms-migration-journal-articles'
		),
		'journal-folders': Liferay.Language.get('cms-migration-folders'),
		'object-entries': Liferay.Language.get('cms-migration-object-entries'),
		'pages': Liferay.Language.get('cms-migration-pages'),
		'repeatable-groups': Liferay.Language.get(
			'cms-migration-repeatable-groups'
		),
		'templates': Liferay.Language.get('cms-migration-templates'),
	};

	return labels[key] ?? key;
}
