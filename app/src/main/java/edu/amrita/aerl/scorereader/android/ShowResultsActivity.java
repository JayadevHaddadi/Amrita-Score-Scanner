package edu.amrita.aerl.scorereader.android;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import amritacard.android.R;
import edu.amrita.aerl.jayadev.util.Printer;
import edu.amrita.aerl.scorereader.core.SheetConfig;
import edu.amrita.aerl.scorereader.core.SheetConfigNew2;
import edu.amrita.aerl.scorereader.core.SheetReaderAndroid;
import edu.amrita.aerl.scorereader.core.SheetReaderListener;
import edu.amrita.aerl.scorereader.core.support.CharWithConf;

/**
 * Made at Amrita E-learning Research Lab
 *
 * Created by mreza on 30-Jan-18.
 */

public class ShowResultsActivity extends Activity implements SheetReaderListener {
    private int COLOR_CORRECT;
    private int COLOR_CORRECTED;
    private RelativeLayout mainLayout;
    private CharWithConf[][] allChars;
    private Button[][] editButtons;
    private Button[] digitButtons;
    private Button buttonToEdit;
    private CharWithConf charToEdit;
    private int displayWitdh;
    private int displayHeight;
    private SheetConfigNew2 config;
    private int wPerButton;
    private int hPerButton;
    private int updateCol = 0;
    private int updateRow = 0;
    private View.OnClickListener editCharButtonListener;
    private SheetReaderAndroid sheetReader;
    private int rowToEdit;
    private Button[] letterButtons;
    private int rowSumCalculated;
    private int rowSumScanned;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.show_result_activity_layout);

        COLOR_CORRECT = getResources().getColor(R.color.correct);
        COLOR_CORRECTED = getResources().getColor(R.color.corrected);

        getActionBar().hide();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        sheetReader = Preview.sheetReader;

        config = sheetReader.config;
        ImageView resultView = findViewById(R.id.imageView);
        mainLayout = findViewById(R.id.main_layout);
        Display display = getWindowManager().getDefaultDisplay();
        displayWitdh = display.getWidth();
        displayHeight = display.getHeight();
        allChars = new CharWithConf[config.layout.length][config.layout[0].length];

        setupButtons();

        new SaveImageTask().execute(Preview.image);
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(Preview.image, displayWitdh, displayHeight, false);
        resultView.setImageBitmap(resizedBitmap);

        sheetReader.startUpdateTask(Preview.image, this);
    }

    private void setupButtons() {
        editButtons = new Button[config.layout.length][config.layout[0].length];
        wPerButton = displayWitdh / config.layout[0].length;
        hPerButton = displayHeight / config.layout.length;
        editCharButtonListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int row = 0; row < config.layout.length; row++) {
                    for (int col = 0; col < config.layout[0].length; col++) {
                        if (editButtons[row][col] != null && editButtons[row][col].equals(v)) {
                            editChar(row, col);
                            return;
                        }
                    }
                }
            }
        };

        View.OnClickListener onEditDoneListenerDigits = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setAllButtonsVisable();

                for (int bb = 0; bb < digitButtons.length; bb++) {
                    digitButtons[bb].setVisibility(View.GONE);
                }

                for (int b = 0; b < digitButtons.length; b++) {
                    if (digitButtons[b].equals(v)) {
                        charToEdit.setCorrectIndex(b);
                        buttonToEdit.setText(charToEdit.stringValue());
                        checkRow(rowToEdit);
                        return;
                    }
                }
            }
        };
        digitButtons = new Button[10];
        for (int i = 0; i < digitButtons.length; i++) {
            Button button = new Button(this);
            button.setTextColor(Color.BLUE);
            button.setTextSize(20);
            button.setText(String.format("%d", i));
            button.setOnClickListener(onEditDoneListenerDigits);
            button.setVisibility(View.GONE);
            digitButtons[i] = button;
            mainLayout.addView(button);
        }

        View.OnClickListener onEditDoneListenerLetters = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setAllButtonsVisable();

                for (int bb = 0; bb < letterButtons.length; bb++) {
                    letterButtons[bb].setVisibility(View.GONE);
                }

                for (int b = 0; b < letterButtons.length; b++) {
                    if (letterButtons[b].equals(v)) {
                        charToEdit.setCorrectIndex(b);
                        buttonToEdit.setText(charToEdit.stringValue());
                        checkRow(rowToEdit);
                        return;
                    }
                }
            }
        };
        letterButtons = new Button[26];
        for (int i = 0; i < letterButtons.length; i++) {
            Button button = new Button(this);
            button.setTextColor(Color.BLUE);
            button.setTextSize(20);
            button.setText(String.format("%c", SheetReaderAndroid.LETTERS[i]));
            button.setOnClickListener(onEditDoneListenerLetters);
            button.setVisibility(View.GONE);
            letterButtons[i] = button;
            mainLayout.addView(button);
        }
    }

    private void setAllButtonsVisable() {
        for (int i = 0; i < allChars.length; i++) {
            for (int j = 0; j < allChars[0].length; j++) {
                if (editButtons[i][j] != null) {
                    editButtons[i][j].setVisibility(View.VISIBLE);
                    editButtons[i][j].setClickable(true);
                }
            }
        }
    }

    @Override
    public void onUpdateResult(CharWithConf charWith) {
        if (config.layout[updateRow][updateCol] != SheetConfig.SKIP) {
            Button button = new Button(this);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(wPerButton, hPerButton);
            params.setMargins(updateCol * wPerButton, updateRow * hPerButton, 0, 0);
            button.setLayoutParams(params);
            button.setOnClickListener(editCharButtonListener);
            editButtons[updateRow][updateCol] = button;
            mainLayout.addView(button);
            if (charWith != null) {
                button.setText(String.format("%c", charWith.getChar()));
                button.setTextColor(setConfidenceColorSingle(charWith));
                allChars[updateRow][updateCol] = charWith;
            } else {

            }
        }

        /*
        * Checking the sum
        * */
        if (updateCol == config.layout[0].length - 1) {
            if (!checkRow(updateRow))
                tryGuessingCorrect(updateRow);
        }

        updateCol++;
        if (updateCol == config.layout[0].length) {
            updateRow++;
            updateCol = 0;
        }
    }

    private void tryGuessingCorrect(int updateRow) {
        Printer.p("Correction needed");
        double lowestConf = 1;
        int index = -1;
        CharWithConf charToChange = null;
        for (int i = 0; i < allChars[updateRow].length; i++) {
            if (allChars[updateRow][i] == null)
                continue;
            if (allChars[updateRow][i].getConfidence() < lowestConf) {
                lowestConf = allChars[updateRow][i].getConfidence();
                index = i;
                charToChange = allChars[updateRow][i];
            }
        }
        if (config.layout[updateRow][index] == config.DIGIT) {
            Printer.p("Trying to correct");
            rowSumCalculated -= charToChange.getDigit();
            rowSumCalculated += charToChange.getSecond();
            if (rowSumCalculated == rowSumScanned) {
                Printer.p("Corrected to: " + charToChange.getSecond());
                charToChange.setCorrectIndex(charToChange.getSecond());
                editButtons[updateRow][index].setText(charToChange.stringValue());
                editButtons[updateRow][index].invalidate();
                setColorsForRow(updateRow, true);
            } else
                Printer.p("Failed to correct: " + charToChange.getSecond());
        }
    }

    private void setColorsForRow(int row, boolean correct) {
        for (int col = 0; col < config.layout[0].length; col++) {
            if (allChars[row][col] == null)
                continue;
            if (allChars[row][col].isCorrected())
                editButtons[row][col].setTextColor(COLOR_CORRECTED);
            else {
                if (correct)
                    editButtons[row][col].setTextColor(COLOR_CORRECT);
                else
                    editButtons[row][col].setTextColor(setConfidenceColorSingle(allChars[row][col]));
            }
        }
    }

    private int setConfidenceColorSingle(CharWithConf charWithConf) {
        double confPow8 = Math.pow(charWithConf.getConfidence(), 8);
        return 0xFF000000 + ((int) ((1 - confPow8) * 255) << 16) + ((int) (confPow8 * 255));//(int) (confPow8 * COLOR_HIGH_CONF + (1 - confPow8) * COLOR_LOW_CONF); //
    }

    private boolean checkRow(int row) {
        rowSumCalculated = 0;
        rowSumScanned = 0;
        for (int col = 0; col < config.layout[0].length; col++) {
            if (allChars[row][col] != null && config.layout[row][col] == config.DIGIT) {
                rowSumCalculated += allChars[row][col].getDigit();
                Printer.po(allChars[row][col].getDigit() + "(" + allChars[row][col].getProcent() + ") ");
            } else if (allChars[row][col] != null && config.layout[row][col] == config.TOTAL) {
                rowSumScanned = allChars[row][col].getDigit() + rowSumScanned * 10;
            }
        }
        if (rowSumScanned == rowSumCalculated) {
            setColorsForRow(row, true);
        } else {
            setColorsForRow(row, false);
        }
        return rowSumScanned == rowSumCalculated;
    }

    @Override
    public void onFinishUpdate(CharWithConf[][] allChars) {
        Printer.p("Finished!");
        this.allChars = allChars;
        new SaveImageTask().execute(Preview.image);
    }

    private void editChar(int row, int col) {
        Printer.p("Editing: " + row + ", " + col);
        if (allChars[row][col] == null) {
            CharWithConf defaultChar;
            if (config.layout[row][col] == SheetConfig.LETTER)
                defaultChar = new CharWithConf(SheetReaderAndroid.LETTERS);
            else
                defaultChar = new CharWithConf(SheetReaderAndroid.DIGITS);
            allChars[row][col] = defaultChar;
        } else {
            Printer.p("Value: " + allChars[row][col].getChar());
        }

        for (int i = 0; i < allChars.length; i++) {
            for (int j = 0; j < allChars[0].length; j++) {
                if (editButtons[i][j] == null || (i == row && j == col))
                    continue;
                editButtons[i][j].setVisibility(View.GONE);
            }
        }

        editButtons[row][col].setClickable(false);
        buttonToEdit = editButtons[row][col];
        charToEdit = allChars[row][col];
        rowToEdit = row;

        int overOrUnder;
        if (row > (allChars.length / 2))
            overOrUnder = -hPerButton / 2;
        else
            overOrUnder = hPerButton;

        if (config.layout[row][col] == SheetConfig.LETTER) {
            int widthPerBox = displayWitdh / 13;
            int nextRow = 0;
            for (int i = 0; i < letterButtons.length; i++) {
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                params.setMargins(widthPerBox * (i - nextRow), editButtons[row][col].getTop() + overOrUnder,
                        0, 0);
                letterButtons[i].setLayoutParams(params);
                letterButtons[i].setVisibility(View.VISIBLE);
                if (i == 12) {
                    nextRow = 13;
                    overOrUnder *= 1.5;
                }
            }
        } else {
            int widthPerBox = displayWitdh / 10;
            for (int i = 0; i < digitButtons.length; i++) {
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                params.setMargins(widthPerBox * i, editButtons[row][col].getTop() + overOrUnder,
                        0, 0);
                digitButtons[i].setLayoutParams(params);
                digitButtons[i].setVisibility(View.VISIBLE);
            }
        }
    }

    public void saveResults(View view) {
        finish();
    }

    private void saveImageToFile(Bitmap image) {
        File pictureFile = getOutputMediaFile();
        FileOutputStream out = null;
        try {

//            Bitmap original = BitmapFactory.decodeStream(getAssets().open("1024x768.jpg"));
            ByteArrayOutputStream out2 = new ByteArrayOutputStream();
            image.compress(Bitmap.CompressFormat.PNG, 100, out2);
            out = new FileOutputStream(pictureFile);
            out2.writeTo(out);

////            image.
////                image.compress(Bitmap.CompressFormat.JPEG, 90, out); // bmp is your Bitmap instance
//            image.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
            // PNG is a lossless format, the compression factor (100) is ignored
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private File getOutputMediaFile() {
        File sdCard = Environment.getExternalStorageDirectory();
        File dir = new File(sdCard.getAbsolutePath() + "/userclicks");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
                .format(new Date());
        File mediaFile;
        mediaFile = new File(dir.getPath() + File.separator
                + "IMG_" + timeStamp + ".png");

        return mediaFile;
    }

    public void discradResults(View view) {
        finish();
    }

    class SaveImageTask extends AsyncTask<Bitmap, Void, Void> {
        @Override
        protected Void doInBackground(Bitmap... image) {
            saveImageToFile(image[0]);
            return null;
        }
    }
}
