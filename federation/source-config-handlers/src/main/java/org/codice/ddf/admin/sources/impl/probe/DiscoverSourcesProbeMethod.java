/**
 * Copyright (c) Codice Foundation
 * <p>
 * This is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or any later version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details. A copy of the GNU Lesser General Public License
 * is distributed along with this program and can be found at
 * <http://www.gnu.org/licenses/lgpl.html>.
 */
package org.codice.ddf.admin.sources.impl.probe;

import static org.codice.ddf.admin.api.config.federation.SourceConfiguration.HOSTNAME;
import static org.codice.ddf.admin.api.config.federation.SourceConfiguration.PASSWORD;
import static org.codice.ddf.admin.api.config.federation.SourceConfiguration.PORT;
import static org.codice.ddf.admin.api.config.federation.SourceConfiguration.USERNAME;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.codice.ddf.admin.api.config.federation.SourceConfiguration;
import org.codice.ddf.admin.api.handler.ConfigurationMessage;
import org.codice.ddf.admin.api.handler.SourceConfigurationHandler;
import org.codice.ddf.admin.api.handler.method.ProbeMethod;
import org.codice.ddf.admin.api.handler.report.ProbeReport;

import com.google.common.collect.ImmutableMap;

public class DiscoverSourcesProbeMethod extends ProbeMethod<SourceConfiguration>{

    public static final String DISCOVER_SOURCES_ID = "discover-sources";
    public static final String DESCRIPTION = "Retrieves possible configurations for the specified url.";

    // TODO: tbatie - 1/15/17 - These descriptions should be done in the SourceConfiguration
    public static final Map<String, String> REQUIRED_FIELDS =
            ImmutableMap.of(HOSTNAME, "Host name of url the source resides.",
                    PORT, "Port of url the source resides.");

    public static final Map<String, String> OPTIONAL_FIELDS = ImmutableMap.of(USERNAME, "User name to include in request.",
            PASSWORD, "Password of user to include in request.");

    public static final Map<String, String> RETURN_TYPES = ImmutableMap.of(DISCOVER_SOURCES_ID, "List of sources configurations created using the specified url.");

    private List<SourceConfigurationHandler> handlers;

    // TODO: tbatie - 1/15/17 - finish failure and success types
    public DiscoverSourcesProbeMethod(List<SourceConfigurationHandler> handlers) {
        super(DISCOVER_SOURCES_ID, DESCRIPTION,
                REQUIRED_FIELDS,
                OPTIONAL_FIELDS,
                null,
                null,
                null,
                RETURN_TYPES
                );
        this.handlers = handlers;
    }
    @Override
    public ProbeReport probe(SourceConfiguration config) {
        ProbeReport sourcesProbeReport = new ProbeReport();

        List<ProbeReport> sourceProbeReports = handlers.stream()
                .map(handler -> handler.probe(DISCOVER_SOURCES_ID, config))
                .collect(Collectors.toList());

        List<Object> discoveredSources = sourceProbeReports.stream()
                .filter(probeReport -> !probeReport.containsUnsuccessfulMessages())
                .map(report -> report.getProbeResults()
                        .get(DISCOVER_SOURCES_ID))
                .collect(Collectors.toList());

        List<ConfigurationMessage> probeSourceMessages = sourceProbeReports.stream()
                .filter(probeReport -> !probeReport.containsFailureMessages())
                .map(ProbeReport::messages)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

        sourcesProbeReport.probeResult(DISCOVER_SOURCES_ID, discoveredSources).messages(probeSourceMessages);
        return sourcesProbeReport;
    }
}