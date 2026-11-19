/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {render, screen, within} from '@testing-library/react';
import React from 'react';

import '@testing-library/jest-dom';

import MigrationBar, {
	IMigrationBar,
} from '../../src/main/resources/META-INF/resources/js/components/MigrationBar';

function renderMigrationBars(bars: IMigrationBar[]) {
	const numberFormat = new Intl.NumberFormat('en-US');

	return render(
		<>
			{bars.map((bar) => (
				<MigrationBar
					bar={bar}
					key={bar.key}
					numberFormat={numberFormat}
				/>
			))}
		</>
	);
}

describe('MigrationBar', () => {
	it('shows the progress of the bar as computed by the server', () => {
		renderMigrationBars([
			{
				completed: 10,
				current: true,
				key: 'web-content',
				label: 'Web Content',
				pending: false,
				phases: [],
				progress: 99,
				total: 10,
			},
		]);

		expect(screen.getByRole('progressbar')).toHaveAttribute(
			'aria-valuenow',
			'99'
		);
		expect(screen.getByText('Web Content')).not.toHaveClass('text-success');
	});

	it('highlights the current bar, marks the complete bars, and dims the bars that have not started', () => {
		renderMigrationBars([
			{
				completed: 2,
				current: false,
				key: 'documents-and-media',
				label: 'Documents and Media',
				pending: false,
				phases: [],
				progress: 100,
				total: 2,
			},
			{
				completed: 4,
				current: true,
				key: 'web-content',
				label: 'Web Content',
				pending: false,
				phases: [],
				progress: 40,
				total: 10,
			},
			{
				completed: 0,
				current: false,
				key: 'update-pages',
				label: 'Update Pages',
				pending: false,
				phases: [],
				progress: 0,
				total: 5,
			},
		]);

		expect(screen.getByText('Documents and Media')).toHaveClass(
			'text-success'
		);
		expect(screen.getByText('100%')).toBeInTheDocument();
		expect(screen.getByText('Web Content')).toHaveClass('text-info');
		expect(screen.getAllByRole('progressbar')[1]).toHaveAttribute(
			'aria-valuenow',
			'40'
		);
		expect(screen.getByText('Update Pages')).toHaveClass('text-secondary');
	});

	it('shows the phases of the bar and marks the totals still being counted', () => {
		renderMigrationBars([
			{
				completed: 4,
				current: true,
				key: 'web-content',
				label: 'Web Content',
				pending: true,
				phases: [
					{
						completed: 4,
						current: true,
						key: 'journal-articles',
						pending: false,
						total: 10,
					},
					{
						completed: 2,
						current: false,
						key: 'collections',
						pending: false,
						total: 2,
					},
					{
						completed: 0,
						current: false,
						key: 'templates',
						pending: true,
						total: 0,
					},
				],
				progress: 40,
				total: 10,
			},
		]);

		expect(screen.getByText('4 / 10+')).toHaveAttribute(
			'title',
			'cms-migration-total-pending'
		);

		const [articles, collections, templates] = within(
			screen.getByRole('list')
		).getAllByRole('listitem');

		expect(articles).toHaveTextContent(
			'cms-migration-journal-articles 4 / 10'
		);
		expect(articles).toHaveClass('text-info');
		expect(articles).not.toHaveAttribute('title');
		expect(collections).toHaveTextContent('collections 2 / 2');
		expect(templates).toHaveTextContent('cms-migration-templates 0 / …');
		expect(templates).not.toHaveClass('text-info');
		expect(templates).toHaveAttribute(
			'title',
			'cms-migration-total-pending'
		);
	});
});
