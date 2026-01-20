package ci553.happyshop.client.picker;

import ci553.happyshop.utility.AudioManager;
import ci553.happyshop.utility.SoundEffect;

import java.io.IOException;

public class PickerController {
    public PickerModel pickerModel;

    public void doProgressing() throws IOException {
        AudioManager.getInstance().playEffect(SoundEffect.BUTTON_CLICK);
        pickerModel.doProgressing();
    }
    public void doCollected() throws IOException {
        AudioManager.getInstance().playEffect(SoundEffect.BUTTON_CLICK);
        pickerModel.doCollected();
    }
}
