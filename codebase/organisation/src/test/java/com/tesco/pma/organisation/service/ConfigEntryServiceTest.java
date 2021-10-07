package com.tesco.pma.organisation.service;

import com.tesco.pma.organisation.api.ConfigEntry;
import com.tesco.pma.organisation.api.ConfigEntryResponse;
import com.tesco.pma.organisation.api.ConfigEntryType;
import com.tesco.pma.organisation.api.WorkingConfigEntry;
import com.tesco.pma.organisation.dao.ConfigEntryDAO;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ConfigEntryServiceTest {

    private static final UUID ROOT_UUID = UUID.randomUUID();
    private static final UUID CHILD_UUID_1 = UUID.randomUUID();
    private static final UUID CHILD_UUID_2 = UUID.randomUUID();
    private static final UUID CHILD_UUID_1_1 = UUID.randomUUID();

    private final ConfigEntry ROOT_CE = getConfigEntry(ROOT_UUID, "root", null);
    private final ConfigEntry CHILD_CE_1 = getConfigEntry(CHILD_UUID_1, "child_1", ROOT_UUID);
    private final ConfigEntry CHILD_CE_2 = getConfigEntry(CHILD_UUID_2, "child_2", ROOT_UUID);
    private final ConfigEntry CHILD_CE_1_1 = getConfigEntry(CHILD_UUID_1_1, "child_1_1", CHILD_UUID_1);
    private final ConfigEntryDAO dao = Mockito.mock(ConfigEntryDAO.class);

    private final ConfigEntryService service = new ConfigEntryServiceImpl(dao);


    @Test
    void getStructure() {
        Mockito.when(dao.getFullStructure(CHILD_UUID_1)).thenReturn(new HashSet<>(Set.of(ROOT_CE, CHILD_CE_1, CHILD_CE_2, CHILD_CE_1_1)));

        var structure = service.getStructure(CHILD_UUID_1);

        assertEquals(ROOT_UUID, structure.getUuid());
        var children = structure.getChildren();
        assertEquals(children.size(), 2);
        var uuids = children.stream().map(ConfigEntryResponse::getUuid).collect(Collectors.toList());
        assertTrue(uuids.contains(CHILD_UUID_1));
        assertTrue(uuids.contains(CHILD_UUID_2));
        assertEquals(CHILD_UUID_1_1, children.stream().filter(s -> s.getUuid().equals(CHILD_UUID_1)).findFirst().get().getChildren().get(0).getUuid());
    }

    @Test
    void generateCompositeKey() {
        Mockito.when(dao.findConfigEntryParentStructure(CHILD_UUID_1_1)).thenReturn(new ArrayList<>(List.of(ROOT_CE, CHILD_CE_1, CHILD_CE_1_1)));

        var compositeKey = service.generateCompositeKey(CHILD_UUID_1_1);

        assertEquals("BU/root/BU/child_1/BU/child_1_1/#v4", compositeKey);
    }

    @Test
    void getPublishedChildStructureByCompositeKey() {
        Mockito.when(dao.findPublishedConfigEntriesByKey("BU/root/BU/child_1%/#v4")).thenReturn(new ArrayList<>(List.of(ROOT_CE, CHILD_CE_1, CHILD_CE_2, CHILD_CE_1_1)));

        var structure = service.getPublishedChildStructureByCompositeKey("BU/root/BU/child_1/#v4");

        assertEquals(ROOT_UUID, structure.getUuid());
        var children = structure.getChildren();
        assertEquals(children.size(), 2);
        var uuids = children.stream().map(ConfigEntryResponse::getUuid).collect(Collectors.toList());
        assertTrue(uuids.contains(CHILD_UUID_1));
        assertTrue(uuids.contains(CHILD_UUID_2));
        assertEquals(CHILD_UUID_1_1, children.stream().filter(s -> s.getUuid().equals(CHILD_UUID_1)).findFirst().get().getChildren().get(0).getUuid());
    }

    @Test
    void publishConfigEntries() {
        Mockito.when(dao.findConfigEntryParentStructure(CHILD_UUID_1)).thenReturn(new ArrayList<>(List.of(ROOT_CE, CHILD_CE_1)));
        Mockito.when(dao.findConfigEntryChildStructure(CHILD_UUID_1)).thenReturn(new ArrayList<>(List.of(CHILD_CE_1, CHILD_CE_1_1)));
        Mockito.when(dao.getFullStructure(CHILD_UUID_1)).thenReturn(new HashSet<>(List.of(ROOT_CE, CHILD_CE_1, CHILD_CE_2, CHILD_CE_1_1)));

        service.publishConfigEntry(CHILD_UUID_1);

        Mockito.verify(dao).unpublishConfigEntries("BU/root/BU/child_1%");
        Mockito.verify(dao).publishConfigEntry(getWorkingConfigEntry(CHILD_UUID_1, "child_1", "BU/root/BU/child_1/#v4"));
        Mockito.verify(dao).publishConfigEntry(getWorkingConfigEntry(CHILD_UUID_1_1, "child_1_1", "BU/root/BU/child_1/BU/child_1_1/#v4"));
    }

    @Test
    void unpublishConfigEntries() {
        Mockito.when(dao.findConfigEntryParentStructure(CHILD_UUID_1_1)).thenReturn(new ArrayList<>(List.of(ROOT_CE, CHILD_CE_1, CHILD_CE_1_1)));

        service.unpublishConfigEntry(CHILD_UUID_1_1);

        Mockito.verify(dao).unpublishConfigEntries("BU/root/BU/child_1/BU/child_1_1%");
    }

    @Test
    void createConfigEntryWithoutParent() {
        service.createConfigEntry(new ConfigEntry());
        Mockito.verify(dao).createConfigEntry(Mockito.argThat(a -> a.getVersion() == 1));
        Mockito.verifyNoMoreInteractions(dao);
    }

    @Test
    void createConfigEntryWithParent() {
        Mockito.when(dao.findRootConfigEntry(ROOT_UUID)).thenReturn(getConfigEntry(ROOT_UUID, "root", null));
        Mockito.when(dao.findConfigEntryChildStructure(ROOT_UUID)).thenReturn(new ArrayList<>(List.of(ROOT_CE, CHILD_CE_1, CHILD_CE_1_1)));

        service.createConfigEntry(getConfigEntry(CHILD_UUID_1, "child", ROOT_UUID));

        Mockito.verify(dao, Mockito.times(4)).createConfigEntry(Mockito.argThat(a -> a.getVersion() == 5));
    }

    @Test
    void updateConfigEntry() {
        Mockito.when(dao.findRootConfigEntry(CHILD_UUID_1)).thenReturn(getConfigEntry(ROOT_UUID, "root", null));
        Mockito.when(dao.findConfigEntryChildStructure(ROOT_UUID)).thenReturn(new ArrayList<>(List.of(ROOT_CE, CHILD_CE_1, CHILD_CE_1_1)));

        service.updateConfigEntry(getConfigEntry(CHILD_UUID_1, "child_updated", null));

        Mockito.verify(dao, Mockito.times(3)).createConfigEntry(Mockito.argThat(a -> a.getVersion() == 5));
        Mockito.verify(dao, Mockito.times(2)).createConfigEntry(Mockito.argThat(a -> a.getParentUuid() == null));
        Mockito.verify(dao).createConfigEntry(Mockito.argThat(a -> a.getParentUuid() == null && a.getName().equals("child_updated")));
    }

    @Test
    void deleteConfigEntry() {
        Mockito.when(dao.findRootConfigEntry(CHILD_UUID_1)).thenReturn(getConfigEntry(ROOT_UUID, "root", null));
        Mockito.when(dao.findConfigEntryChildStructure(ROOT_UUID)).thenReturn(new ArrayList<>(List.of(ROOT_CE, CHILD_CE_1, CHILD_CE_1_1)));

        service.deleteConfigEntry(CHILD_UUID_1);

        Mockito.verify(dao).createConfigEntry(Mockito.argThat(a -> a.getVersion() == 5));
    }

    private ConfigEntry getConfigEntry(UUID uuid, String name, UUID parentUuid) {
        var cet = getConfigEntryType();

        var ce = new ConfigEntry();
        ce.setUuid(uuid);
        ce.setName(name);
        ce.setType(cet);
        ce.setVersion(4);
        ce.setParentUuid(parentUuid);
        return ce;
    }

    private WorkingConfigEntry getWorkingConfigEntry(UUID uuid, String name, String key) {
        var cet = getConfigEntryType();

        var ce = new WorkingConfigEntry();
        ce.setName(name);
        ce.setType(cet);
        ce.setConfigEntryUuid(uuid);
        ce.setCompositeKey(key);
        ce.setVersion(4);
        return ce;
    }


    private ConfigEntryType getConfigEntryType() {
        var cet = new ConfigEntryType();
        cet.setId(1);
        cet.setCode("BU");
        cet.setDescription("desc");
        return cet;
    }
}
