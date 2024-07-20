package ua.dscorp.poessence.util;

import java.io.File;
import java.io.FilenameFilter;

public class TableContentFileFilter implements FilenameFilter {
    private final ItemType itemType;

    public TableContentFileFilter(ItemType itemType) {
        this.itemType = itemType;
    }

    @Override
    public boolean accept(File dir, String name) {
        return name.endsWith("_save.json") && name.startsWith(itemType.getName());
    }
}