package team.creative.littletiles.common.gui.signal;

import java.util.List;

import com.creativemd.creativecore.common.gui.container.SubGui;
import com.creativemd.creativecore.common.gui.controls.gui.GuiCounter;
import com.creativemd.creativecore.common.gui.controls.gui.GuiPanel;
import com.creativemd.creativecore.common.gui.controls.gui.GuiScrollBox;
import com.n247s.api.eventapi.eventsystem.CustomEventSubscribe;

import team.creative.creativecore.common.gui.controls.simple.GuiButton;
import team.creative.creativecore.common.gui.controls.simple.GuiLabel;
import team.creative.creativecore.common.gui.controls.simple.GuiStateButton;
import team.creative.creativecore.common.gui.event.GuiControlChangedEvent;
import team.creative.littletiles.common.gui.signal.GuiSignalController.GuiSignalNodeVirtualInput;
import team.creative.littletiles.common.gui.signal.SubGuiDialogSignal.GuiSignalComponent;
import team.creative.littletiles.common.gui.signal.SubGuiDialogSignal.IConditionConfiguration;
import team.creative.littletiles.common.structure.signal.component.SignalComponentType;
import team.creative.littletiles.common.structure.signal.input.SignalInputCondition;
import team.creative.littletiles.common.structure.signal.input.SignalInputCondition.SignalInputBit;
import team.creative.littletiles.common.structure.signal.logic.SignalMode.GuiSignalModeConfiguration;

public class SubGuiDialogSignalVirtualInput extends SubGui {
    
    public final GuiSignalNodeVirtualInput input;
    public final List<GuiSignalComponent> inputs;
    public GuiVirtualInputIndexConfiguration[] config;
    
    public SubGuiDialogSignalVirtualInput(List<GuiSignalComponent> inputs, GuiSignalNodeVirtualInput input) {
        this.input = input;
        this.inputs = inputs;
    }
    
    @Override
    public void createControls() {
        controls.add(new GuiCounter("bandwidth", 0, 0, 40, input.conditions.length, 0, 256));
        controls.add(new GuiScrollBox("config", 0, 20, 170, 116));
        controls.add(new GuiButton("cancel", 0, 146) {
            
            @Override
            public void onClicked(int x, int y, int button) {
                closeGui();
            }
        });
        controls.add(new GuiButton("save", 146, 146) {
            
            @Override
            public void onClicked(int x, int y, int button) {
                input.conditions = new SignalInputCondition[config.length];
                for (int i = 0; i < config.length; i++)
                    input.conditions[i] = config[i].parse();
                input.updateLabel();
                closeGui();
            }
        });
        loadConditions();
    }
    
    @CustomEventSubscribe
    public void changed(GuiControlChangedEvent event) {
        if (event.source.is("bandwidth"))
            loadConditions();
    }
    
    public void loadConditions() {
        GuiScrollBox box = (GuiScrollBox) get("config");
        box.controls.clear();
        GuiCounter counter = (GuiCounter) get("bandwidth");
        int bandwidth = counter.getValue();
        config = new GuiVirtualInputIndexConfiguration[bandwidth];
        for (int i = 0; i < bandwidth; i++) {
            GuiVirtualInputIndexConfiguration index = new GuiVirtualInputIndexConfiguration(i < input.conditions.length ? input.conditions[i] : new SignalInputBit(false), i);
            index.create(box);
            config[i] = index;
        }
    }
    
    public class GuiVirtualInputIndexConfiguration implements IConditionConfiguration {
        
        public final GuiSignalComponent output;
        public final int index;
        public SignalInputCondition condition;
        public GuiPanel panel;
        
        public GuiVirtualInputIndexConfiguration(SignalInputCondition condition, int index) {
            this.output = new GuiSignalComponent("" + index, "" + index, 1, SignalComponentType.OUTPUT, false, index);
            this.index = index;
            this.condition = condition;
        }
        
        public void create(GuiScrollBox box) {
            panel = new GuiPanel(index + "", 0, index * 24, 162, 20);
            panel.addControl(new GuiLabel("label", index + ": " + (condition != null ? condition.write() : "0"), 0, 3));
            int state = 0;
            if (condition instanceof SignalInputBit)
                state = ((SignalInputBit) condition).bit ? 1 : 0;
            else
                state = 2;
            panel.addControl(new GuiStateButton("type", state, 90, 0, 40, "false", "true", "equation") {
                @Override
                public void onClicked(int x, int y, int button) {
                    update();
                }
            });
            panel.addControl(new GuiButton("edit", 138, 0) {
                
                @Override
                public void onClicked(int x, int y, int button) {
                    openClientLayer(new SubGuiDialogSignal(inputs, GuiVirtualInputIndexConfiguration.this));
                }
            });
            box.addControl(panel);
            update();
        }
        
        @Override
        public void update() {
            GuiLabel label = (GuiLabel) panel.get("label");
            GuiStateButton type = (GuiStateButton) panel.get("type");
            GuiButton edit = (GuiButton) panel.get("edit");
            
            label.setCaption(index + ": " + parse().write());
            edit.setEnabled(type.getState() == 2);
        }
        
        public SignalInputCondition parse() {
            GuiStateButton type = (GuiStateButton) panel.get("type");
            if (type.getState() == 0)
                return new SignalInputBit(false);
            else if (type.getState() == 1)
                return new SignalInputBit(true);
            if (condition != null)
                return condition;
            return new SignalInputBit(false);
        }
        
        @Override
        public GuiSignalComponent getOutput() {
            return output;
        }
        
        @Override
        public SignalInputCondition getCondition() {
            return condition;
        }
        
        @Override
        public void setCondition(SignalInputCondition condition) {
            this.condition = condition;
        }
        
        @Override
        public boolean hasModeConfiguration() {
            return false;
        }
        
        @Override
        public GuiSignalModeConfiguration getModeConfiguration() {
            return null;
        }
        
        @Override
        public void setModeConfiguration(GuiSignalModeConfiguration config) {
            
        }
        
    }
    
}
