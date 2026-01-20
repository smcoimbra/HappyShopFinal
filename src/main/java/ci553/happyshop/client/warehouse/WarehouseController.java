package ci553.happyshop.client.warehouse;

import ci553.happyshop.utility.AudioManager;
import ci553.happyshop.utility.SoundEffect;

import java.io.IOException;
import java.sql.SQLException;

public class WarehouseController {
    public WarehouseModel model;

    void process(String action) throws SQLException, IOException {
        // Play button click sound for all button actions
        AudioManager.getInstance().playEffect(SoundEffect.BUTTON_CLICK);

        switch (action) {
            case "🔍":
                model.doSearch();
                break;
            case "Edit":
                model.doEdit();
                break;
            case "Delete":
                model.doDelete();
                break;
            case "➕":
                model.doChangeStockBy("add");
                break;
            case "➖":
                model.doChangeStockBy("sub");
                break;
            case "Submit":
                model.doSummit();
                break;
            case "Cancel":  // clear the editChild
                model.doCancel();
                break;
        }
    }
}
