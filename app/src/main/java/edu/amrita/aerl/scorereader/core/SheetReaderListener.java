package edu.amrita.aerl.scorereader.core;

import edu.amrita.aerl.scorereader.core.support.CharWithConf;

/**
 * Made at Amrita E-learning Research Lab
 *
 * Created by mreza on 30-Jan-18.
 */

public interface SheetReaderListener {
    void onUpdateResult(CharWithConf charWithConf);
//    void onUpdateResult();
    void onFinishUpdate(CharWithConf[][] charWithConfs);
}
