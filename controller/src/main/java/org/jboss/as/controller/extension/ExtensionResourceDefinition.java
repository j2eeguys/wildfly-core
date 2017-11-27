/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.as.controller.extension;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.EXTENSION;

import org.jboss.as.controller.ModelVersion;
import org.jboss.as.controller.OperationDefinition;
import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.SimpleAttributeDefinition;
import org.jboss.as.controller.SimpleAttributeDefinitionBuilder;
import org.jboss.as.controller.SimpleOperationDefinitionBuilder;
import org.jboss.as.controller.SimpleResourceDefinition;
import org.jboss.as.controller.access.management.SensitiveTargetAccessConstraintDefinition;
import org.jboss.as.controller.descriptions.ModelDescriptionConstants;
import org.jboss.as.controller.descriptions.common.ControllerResolver;
import org.jboss.as.controller.operations.validation.StringLengthValidator;
import org.jboss.as.controller.registry.ManagementResourceRegistration;
import org.jboss.as.controller.registry.OperationEntry;
import org.jboss.dmr.ModelType;

/**
 * {@link SimpleResourceDefinition} for an {@link org.jboss.as.controller.Extension} resource.
 *
 * @author Brian Stansberry (c) 2011 Red Hat Inc.
 */
public class ExtensionResourceDefinition extends SimpleResourceDefinition {

    public static final SimpleAttributeDefinition MODULE = new SimpleAttributeDefinitionBuilder(ModelDescriptionConstants.MODULE, ModelType.STRING, false)
            .setValidator(new StringLengthValidator(1)).build();

    private static final OperationDefinition ADD_OP = new SimpleOperationDefinitionBuilder(ADD, ControllerResolver.getResolver(EXTENSION))
            .addParameter(new SimpleAttributeDefinitionBuilder(ModelDescriptionConstants.MODULE, ModelType.STRING, true)
                    .setValidator(new StringLengthValidator(1))
                    .setDeprecated(ModelVersion.create(6))
                    .build())
            .build();

    private final ExtensionAddHandler addHandler;

    public ExtensionResourceDefinition(final ExtensionRegistry extensionRegistry, final boolean parallelBoot,
                                       final ExtensionRegistryType extensionRegistryType, final MutableRootResourceRegistrationProvider rootResourceRegistrationProvider) {
        super(new Parameters(PathElement.pathElement(EXTENSION), ControllerResolver.getResolver(EXTENSION))
                .setAccessConstraints(SensitiveTargetAccessConstraintDefinition.EXTENSIONS)
                .setRemoveHandler(new ExtensionRemoveHandler(extensionRegistry, extensionRegistryType, rootResourceRegistrationProvider))
                .setAddRestartLevel(OperationEntry.Flag.RESTART_NONE)
                .setRemoveRestartLevel(OperationEntry.Flag.RESTART_NONE));
        this.addHandler = new ExtensionAddHandler(extensionRegistry, parallelBoot, extensionRegistryType, rootResourceRegistrationProvider);
    }

    @Override
    public void registerOperations(ManagementResourceRegistration resourceRegistration) {
        super.registerOperations(resourceRegistration);
        resourceRegistration.registerOperationHandler(ADD_OP, addHandler);
    }

    @Override
    public void registerAttributes(ManagementResourceRegistration resourceRegistration) {
        resourceRegistration.registerReadOnlyAttribute(MODULE, null);
    }

    @Override
    public void registerChildren(ManagementResourceRegistration resourceRegistration) {
        resourceRegistration.registerSubModel(new ExtensionSubsystemResourceDefinition());
    }
}
