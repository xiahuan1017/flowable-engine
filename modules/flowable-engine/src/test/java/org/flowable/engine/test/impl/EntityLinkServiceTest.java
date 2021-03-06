/* Licensed under the Apache License, Version 2.0 (the "License");
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
package org.flowable.engine.test.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import java.util.List;

import org.flowable.common.engine.api.scope.ScopeTypes;
import org.flowable.engine.impl.test.PluggableFlowableTestCase;
import org.flowable.engine.impl.util.CommandContextUtil;
import org.flowable.entitylink.api.EntityLink;
import org.flowable.entitylink.api.EntityLinkInfo;
import org.flowable.entitylink.api.EntityLinkService;
import org.flowable.entitylink.api.EntityLinkType;
import org.flowable.entitylink.api.HierarchyType;
import org.flowable.entitylink.api.history.HistoricEntityLink;
import org.flowable.entitylink.api.history.HistoricEntityLinkService;
import org.flowable.entitylink.service.impl.persistence.entity.EntityLinkEntity;
import org.flowable.entitylink.service.impl.persistence.entity.HistoricEntityLinkEntity;
import org.junit.jupiter.api.Test;

/**
 * @author Filip Hrisafov
 */
public class EntityLinkServiceTest extends PluggableFlowableTestCase {

    @Test
    void testFindEntityLinksWithSameRootWithNullRoot() {
        managementService.executeCommand(commandContext -> {
            EntityLinkService entityLinkService = CommandContextUtil.getEntityLinkService(commandContext);
            HistoricEntityLinkService historicEntityLinkService = CommandContextUtil.getHistoricEntityLinkService();
            createEntityLinkWithoutRootScope("1", "2.1", HierarchyType.ROOT, entityLinkService, historicEntityLinkService);
            createEntityLinkWithoutRootScope("1", "2.2", HierarchyType.ROOT, entityLinkService, historicEntityLinkService);
            createEntityLinkWithoutRootScope("1", "3.1", HierarchyType.ROOT, entityLinkService, historicEntityLinkService);
            createEntityLinkWithoutRootScope("1", "3.2", HierarchyType.ROOT, entityLinkService, historicEntityLinkService);
            createEntityLinkWithoutRootScope("2.1", "3.1", HierarchyType.PARENT, entityLinkService, historicEntityLinkService);
            createEntityLinkWithoutRootScope("2.1", "3.2", HierarchyType.PARENT, entityLinkService, historicEntityLinkService);

            return null;
        });

        assertThat(findEntityLinksByScopeId("1"))
                .extracting(EntityLinkInfo::getScopeId, EntityLinkInfo::getReferenceScopeId, EntityLinkInfo::getHierarchyType)
                .containsExactlyInAnyOrder(
                        tuple("1", "2.1", HierarchyType.ROOT),
                        tuple("1", "2.2", HierarchyType.ROOT),
                        tuple("1", "3.1", HierarchyType.ROOT),
                        tuple("1", "3.2", HierarchyType.ROOT)
                );

        assertThat(findEntityLinksByScopeId("2.1"))
                .extracting(EntityLinkInfo::getScopeId, EntityLinkInfo::getReferenceScopeId, EntityLinkInfo::getHierarchyType)
                .containsExactlyInAnyOrder(
                        tuple("2.1", "3.1", HierarchyType.PARENT),
                        tuple("2.1", "3.2", HierarchyType.PARENT)
                );

        assertThat(findEntityLinksByScopeId("2.2"))
                .extracting(EntityLinkInfo::getScopeId, EntityLinkInfo::getReferenceScopeId, EntityLinkInfo::getHierarchyType)
                .isEmpty();

        assertThat(findEntityLinksWithSameRootByScopeId("1"))
                .extracting(EntityLinkInfo::getScopeId, EntityLinkInfo::getReferenceScopeId, EntityLinkInfo::getHierarchyType)
                .isEmpty();

        assertThat(findEntityLinksWithSameRootByScopeId("2.1"))
                .extracting(EntityLinkInfo::getScopeId, EntityLinkInfo::getReferenceScopeId, EntityLinkInfo::getHierarchyType)
                .isEmpty();

        assertThat(findHistoricEntityLinksByScopeId("1"))
                .extracting(EntityLinkInfo::getScopeId, EntityLinkInfo::getReferenceScopeId, EntityLinkInfo::getHierarchyType)
                .containsExactlyInAnyOrder(
                        tuple("1", "2.1", HierarchyType.ROOT),
                        tuple("1", "2.2", HierarchyType.ROOT),
                        tuple("1", "3.1", HierarchyType.ROOT),
                        tuple("1", "3.2", HierarchyType.ROOT)
                );

        assertThat(findHistoricEntityLinksByScopeId("2.1"))
                .extracting(EntityLinkInfo::getScopeId, EntityLinkInfo::getReferenceScopeId, EntityLinkInfo::getHierarchyType)
                .containsExactlyInAnyOrder(
                        tuple("2.1", "3.1", HierarchyType.PARENT),
                        tuple("2.1", "3.2", HierarchyType.PARENT)
                );

        assertThat(findHistoricEntityLinksByScopeId("2.2"))
                .extracting(EntityLinkInfo::getScopeId, EntityLinkInfo::getReferenceScopeId, EntityLinkInfo::getHierarchyType)
                .isEmpty();

        assertThat(findHistoricEntityLinksWithSameRootByScopeId("1"))
                .extracting(EntityLinkInfo::getScopeId, EntityLinkInfo::getReferenceScopeId, EntityLinkInfo::getHierarchyType)
                .isEmpty();

        assertThat(findHistoricEntityLinksWithSameRootByScopeId("2.1"))
                .extracting(EntityLinkInfo::getScopeId, EntityLinkInfo::getReferenceScopeId, EntityLinkInfo::getHierarchyType)
                .isEmpty();

        managementService.executeCommand(commandContext -> {
            EntityLinkService entityLinkService = CommandContextUtil.getEntityLinkService(commandContext);
            entityLinkService.deleteEntityLinksByScopeIdAndType("1", ScopeTypes.BPMN);
            entityLinkService.deleteEntityLinksByScopeIdAndType("2.1", ScopeTypes.BPMN);

            HistoricEntityLinkService historicEntityLinkService = CommandContextUtil.getHistoricEntityLinkService();
            historicEntityLinkService.deleteHistoricEntityLinksByScopeIdAndScopeType("1", ScopeTypes.BPMN);
            historicEntityLinkService.deleteHistoricEntityLinksByScopeIdAndScopeType("2.1", ScopeTypes.BPMN);
            return null;
        });
    }

    protected List<EntityLink> findEntityLinksByScopeId(String scopeId) {
        return managementService.executeCommand(commandContext -> CommandContextUtil.getEntityLinkService(commandContext)
                .findEntityLinksByScopeIdAndType(scopeId, ScopeTypes.BPMN, EntityLinkType.CHILD));
    }

    protected List<EntityLink> findEntityLinksWithSameRootByScopeId(String scopeId) {
        return managementService.executeCommand(commandContext -> CommandContextUtil.getEntityLinkService(commandContext)
                .findEntityLinksWithSameRootScopeForScopeIdAndScopeType(scopeId, ScopeTypes.BPMN, EntityLinkType.CHILD));
    }

    protected List<HistoricEntityLink> findHistoricEntityLinksByScopeId(String scopeId) {
        return managementService.executeCommand(commandContext -> CommandContextUtil.getHistoricEntityLinkService()
                .findHistoricEntityLinksByScopeIdAndScopeType(scopeId, ScopeTypes.BPMN, EntityLinkType.CHILD));
    }

    protected List<HistoricEntityLink> findHistoricEntityLinksWithSameRootByScopeId(String scopeId) {
        return managementService.executeCommand(commandContext -> CommandContextUtil.getHistoricEntityLinkService()
                .findHistoricEntityLinksWithSameRootScopeForScopeIdAndScopeType(scopeId, ScopeTypes.BPMN, EntityLinkType.CHILD));
    }

    protected void createEntityLinkWithoutRootScope(String scopeId, String referenceId, String hierarchyType, EntityLinkService entityLinkService,
            HistoricEntityLinkService historicEntityLinkService) {

        EntityLinkEntity entityLink = (EntityLinkEntity) entityLinkService.createEntityLink();
        entityLink.setScopeId(scopeId);
        entityLink.setScopeType(ScopeTypes.BPMN);
        entityLink.setLinkType(EntityLinkType.CHILD);
        entityLink.setReferenceScopeId(referenceId);
        entityLink.setReferenceScopeType(ScopeTypes.BPMN);
        entityLink.setHierarchyType(hierarchyType);
        entityLinkService.insertEntityLink(entityLink);

        HistoricEntityLinkEntity historicEntityLink = (HistoricEntityLinkEntity) historicEntityLinkService.createHistoricEntityLink();
        historicEntityLink.setScopeId(scopeId);
        historicEntityLink.setScopeType(ScopeTypes.BPMN);
        historicEntityLink.setLinkType(EntityLinkType.CHILD);
        historicEntityLink.setReferenceScopeId(referenceId);
        historicEntityLink.setReferenceScopeType(ScopeTypes.BPMN);
        historicEntityLink.setHierarchyType(hierarchyType);
        historicEntityLinkService.insertHistoricEntityLink(historicEntityLink, false);
    }
}
