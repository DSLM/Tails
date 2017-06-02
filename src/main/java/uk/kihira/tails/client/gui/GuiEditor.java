/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014
 *
 * See LICENSE for full License
 */

package uk.kihira.tails.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.player.EntityPlayer;
import uk.kihira.tails.client.texture.TextureHelper;
import uk.kihira.tails.common.PartInfo;
import uk.kihira.tails.common.PartsData;
import uk.kihira.tails.common.Tails;

import java.util.UUID;

public class GuiEditor extends GuiBase {

    int textureID;
    private PartsData.PartType partType;
    private PartsData partsData;
    private PartInfo editingPartInfo;
    PartInfo originalPartInfo;
    private UUID playerUUID;

    private int guiScale;

    TintPanel tintPanel;
    PartsPanel partsPanel;
    private PreviewPanel previewPanel;
    TexturePanel texturePanel;
    private ControlsPanel controlsPanel;
    public LibraryPanel libraryPanel;
    public LibraryInfoPanel libraryInfoPanel;
    LibraryImportPanel libraryImportPanel;

    public GuiEditor() {
        super(4);
        //Backup original PartInfo or create default one
        PartInfo partInfo;
        if (Tails.localPartsData == null) {
            Tails.setLocalPartsData(new PartsData());
        }

        //Default to Tail
        partType = PartsData.PartType.TAIL;
        for (PartsData.PartType partType : PartsData.PartType.values()) {
            if (!Tails.localPartsData.hasPartInfo(partType)) {
                Tails.localPartsData.setPartInfo(partType, new PartInfo(false, 0, 0, 0, 0xFFFF0000, 0xFF00FF00, 0xFF0000FF, null, partType));
            }
        }
        partInfo = Tails.localPartsData.getPartInfo(partType);
        playerUUID = EntityPlayer.getUUID(Minecraft.getMinecraft().getSession().getProfile());

        originalPartInfo = partInfo.deepCopy();
        setPartsData(Tails.localPartsData.deepCopy());
        editingPartInfo = originalPartInfo.deepCopy();

        //guiScale = Minecraft.getMinecraft().gameSettings.guiScale;
        //setScale(4);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void initGui() {
        int previewWindowEdgeOffset = 110;
        int previewWindowRight = width - previewWindowEdgeOffset;
        int previewWindowBottom = height - 30;
        int texSelectHeight = 35;

        //Not an ideal solution but keeps everything from resetting on resize
        if (tintPanel == null) {
            getLayer(0).add(previewPanel = new PreviewPanel(this, previewWindowEdgeOffset, 0, previewWindowRight - previewWindowEdgeOffset, previewWindowBottom));
            getLayer(1).add(partsPanel = new PartsPanel(this, 0, 0, previewWindowEdgeOffset, height - texSelectHeight));
            getLayer(1).add(libraryPanel = new LibraryPanel(this, 0, 0, previewWindowEdgeOffset, height));
            getLayer(1).add(texturePanel = new TexturePanel(this, 0, height - texSelectHeight, previewWindowEdgeOffset, 43));
            getLayer(1).add(tintPanel = new TintPanel(this, previewWindowRight, 0, width - previewWindowRight, height));
            getLayer(1).add(libraryImportPanel = new LibraryImportPanel(this, previewWindowRight, height - 60, width - previewWindowRight, 60));
            getLayer(1).add(libraryInfoPanel = new LibraryInfoPanel(this, previewWindowRight, 0, width - previewWindowRight, height - 60));
            getLayer(1).add(controlsPanel = new ControlsPanel(this, previewWindowEdgeOffset, previewWindowBottom, previewWindowRight - previewWindowEdgeOffset, height - previewWindowBottom));

            libraryInfoPanel.enabled = false;
            libraryImportPanel.enabled = false;
            libraryPanel.enabled = false;
        }
        else {
            tintPanel.resize(previewWindowRight, 0, width - previewWindowRight, height);
            libraryInfoPanel.resize(previewWindowRight, 0, width - previewWindowRight, height - 60);
            partsPanel.resize(0, 0, previewWindowEdgeOffset, height - texSelectHeight);
            libraryPanel.resize(0, 0, previewWindowEdgeOffset, height);
            previewPanel.resize(previewWindowEdgeOffset, 0, previewWindowRight - previewWindowEdgeOffset, previewWindowBottom);
            texturePanel.resize(0, height - texSelectHeight, previewWindowEdgeOffset, 43);
            libraryImportPanel.resize(previewWindowRight, height - 60, width - previewWindowRight, 60);
            controlsPanel.resize(previewWindowEdgeOffset, previewWindowBottom, previewWindowRight - previewWindowEdgeOffset, height - previewWindowBottom);
        }
        super.initGui();
    }

    @Override
    public void onGuiClosed() {
        Tails.proxy.addPartsData(playerUUID, Tails.localPartsData);
        //setScale(guiScale);
        super.onGuiClosed();
    }

    void refreshTintPane() {
        tintPanel.refreshTintPane();
    }

    void setPartsInfo(PartInfo newPartInfo) {
        //editingPartInfo.setTexture(null); //Clear texture data as we will no longer need it
        editingPartInfo = newPartInfo;
        if (editingPartInfo.hasPart) editingPartInfo.setTexture(TextureHelper.generateTexture(playerUUID, editingPartInfo));

        partsData.setPartInfo(partType, editingPartInfo);
        setPartsData(partsData);

        texturePanel.updateButtons();
    }

    PartInfo getEditingPartInfo() {
        return editingPartInfo;
    }

    public void setPartsData(PartsData newPartsData) {
        partsData = newPartsData;
        Tails.proxy.addPartsData(playerUUID, partsData);
    }

    public PartsData getPartsData() {
        return partsData;
    }

    public void setPartType(PartsData.PartType partType) {
        this.partType = partType;

        PartInfo newPartInfo = partsData.getPartInfo(partType);
        if (newPartInfo == null) {
            newPartInfo = new PartInfo(false, 0, 0, 0, 0xFFFF0000, 0xFF00FF00, 0xFF0000FF, null, partType);
        }
        originalPartInfo = newPartInfo.deepCopy();
        PartInfo partInfo = originalPartInfo.deepCopy();

        clearCurrTintEdit();
        setPartsInfo(partInfo);
        partsPanel.initPartList();
        refreshTintPane();
        textureID = partInfo.textureID;
        texturePanel.updateButtons();
    }

    public PartsData.PartType getPartType() {
        return partType;
    }

    void clearCurrTintEdit() {
        tintPanel.currTintEdit = 0;
    }

    private void setScale(int scale) {
        Minecraft.getMinecraft().gameSettings.guiScale = scale;
        ScaledResolution scaledresolution = new ScaledResolution(Minecraft.getMinecraft());
        int j = scaledresolution.getScaledWidth();
        int k = scaledresolution.getScaledHeight();
        this.setWorldAndResolution(Minecraft.getMinecraft(), j, k);
    }
}
