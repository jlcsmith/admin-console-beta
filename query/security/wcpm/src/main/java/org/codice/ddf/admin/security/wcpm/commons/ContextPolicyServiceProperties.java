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
 **/
package org.codice.ddf.admin.security.wcpm.commons;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.codice.ddf.admin.api.fields.ListField;
import org.codice.ddf.admin.common.fields.common.ContextPath;
import org.codice.ddf.admin.common.services.ServicesCommons;
import org.codice.ddf.admin.configurator.ConfigReader;
import org.codice.ddf.admin.security.common.fields.wcpm.ContextPolicyBin;

import com.google.common.collect.ImmutableMap;

public class ContextPolicyServiceProperties {
    // --- Policy manager props
    public static final String POLICY_MANAGER_PID =
            "org.codice.ddf.security.policy.context.impl.PolicyManager";

    public static final String AUTH_TYPES = "authenticationTypes";

    public static final String REALMS = "realms";

    public static final String REQUIRED_ATTRIBUTES = "requiredAttributes";

    public static final String WHITE_LIST_CONTEXT = "whiteListContexts";

    public static final String ROOT_CONTEXT_PATH = "/";
    // ---

    public static Map<String, Object> contextPoliciesToPolicyManagerProps(
            ListField<ContextPolicyBin> contextPolicies) {
        List<String> realmsProps = new ArrayList<>();
        List<String> authTypesProps = new ArrayList<>();
        List<String> reqAttrisProps = new ArrayList<>();

        for (ContextPolicyBin bin : contextPolicies.getList()) {
            bin.contexts()
                    .forEach(context -> {
                        realmsProps.add(context + "=" + bin.realm());
                        authTypesProps.add(context + "=" + String.join("|", bin.authTypes()));
                        if (bin.claimsMapping()
                                .isEmpty()) {
                            reqAttrisProps.add(context + "=");
                        } else {
                            reqAttrisProps.add(context + "={" + String.join(";",
                                    bin.claimsMapping()
                                            .entrySet()
                                            .stream()
                                            .map(entry -> entry.getKey() + "=" + entry.getValue())
                                            .collect(Collectors.toList())) + "}");
                        }
                    });
        }

        return ImmutableMap.of(AUTH_TYPES,
                authTypesProps,
                REALMS,
                realmsProps,
                REQUIRED_ATTRIBUTES,
                reqAttrisProps);
    }

    public static Map<String, Object> whiteListToPolicyManagerProps(ListField<ContextPath> contexts) {
        List<String> serviceContexts =
                contexts.getValue() == null ? new ArrayList<>() : contexts.getValue();
        return ImmutableMap.of(WHITE_LIST_CONTEXT, serviceContexts);
    }

    public static List<String> getWhitelistContexts(ConfigReader reader) {
        Object whitelistProp = reader.getConfig(POLICY_MANAGER_PID).get(WHITE_LIST_CONTEXT);

        if(whitelistProp != null && whitelistProp instanceof String[]) {
            return new ServicesCommons().resolveProperties((String[])whitelistProp);
        }

        return new ArrayList<>();
    }
}
