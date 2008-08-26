/*
 * Copyright 2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.grails.plugins.searchable.compass.config;

import org.compass.core.config.CompassConfiguration;
import org.compass.core.config.CompassMappingBinding;
import org.compass.core.util.FieldInvoker;
import org.compass.core.mapping.*;
import org.compass.core.mapping.osem.ClassPropertyMapping;
import org.compass.core.mapping.osem.ClassPropertyMetaDataMapping;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

import java.util.Map;
import java.util.Iterator;

/**
 * Sets Compas settings depending on mappings, eg, enables global spell check setting
 * if any mappings use it
 *
 * @author Maurice Nicholson
 */
public class InferredCompassSettingCompassConfigurator implements SearchableCompassConfigurator {
    private static final Log LOG = LogFactory.getLog(InferredCompassSettingCompassConfigurator.class);
    private static final String COMPASS_SPELL_CHECK_SETTING = "compass.engine.spellcheck.enable";

    public void configure(CompassConfiguration compassConfiguration, Map configurationContext) {
        boolean spellCheck = hasSpellCheckMapping(compassConfiguration);
        if (spellCheck) {
            if (compassConfiguration.getSettings().getSetting(COMPASS_SPELL_CHECK_SETTING) == null) {
                compassConfiguration.setSetting(COMPASS_SPELL_CHECK_SETTING, "true");
                LOG.debug("Eabled Compass global Spell Check setting \"" + COMPASS_SPELL_CHECK_SETTING + "\" since it was not already set and there are spell-check mappings");
            }
        }
    }

    private boolean hasSpellCheckMapping(CompassConfiguration compassConfiguration) {
        try {
            FieldInvoker invoker = new FieldInvoker(CompassConfiguration.class, "mappingBinding").prepare();
            CompassMappingBinding mappingBinding = (CompassMappingBinding) invoker.get(compassConfiguration);

            invoker = new FieldInvoker(CompassMappingBinding.class, "mapping").prepare();
            CompassMapping mapping = (CompassMapping) invoker.get(mappingBinding);

            ResourceMapping[] mappings = mapping.getRootMappings();
            for (int i = 0; i < mappings.length; i++) {
                ResourceMapping resourceMapping = mappings[i];
                if (resourceMapping.getSpellCheck().equals(SpellCheckType.INCLUDE)) {
                    return true;
                }
                for (Iterator iter = resourceMapping.mappingsIt(); iter.hasNext(); ) {
                    Object o = iter.next();
                    if (o instanceof ClassPropertyMapping) {
                        for (Iterator iter2 = ((ClassPropertyMapping) o).mappingsIt(); iter2.hasNext(); ) {
                            Object o2 = iter2.next();
                            if (o2 instanceof ClassPropertyMetaDataMapping) {
                                if (((ClassPropertyMetaDataMapping) o2).getSpellCheck().equals(SpellCheckType.INCLUDE)) {
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
            return false;
        } catch (IllegalAccessException ex) {
            throw new RuntimeException("Failed to get CompassConfiguration#mappingBinding", ex);
        } catch (NoSuchFieldException ex) {
            throw new RuntimeException("Failed to get CompassConfiguration#mappingBinding", ex);
        }
    }
}