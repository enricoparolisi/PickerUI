package com.dpizarro.uipicker.library.picker;

import com.dpizarro.uipicker.library.R;
import com.dpizarro.uipicker.library.blur.PickerUIBlur;
import com.dpizarro.uipicker.library.blur.PickerUIBlurHelper;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Arrays;
import java.util.List;

/*
 * Copyright (C) 2015 David Pizarro
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
public class PickerUI extends RelativeLayout implements PickerUIBlurHelper.BlurFinishedListener {

    private final FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.BOTTOM
    );

    private static final String LOG_TAG = PickerUI.class.getSimpleName();

    private boolean autoDismiss = PickerUISettings.DEFAULT_AUTO_DISMISS;
    private boolean itemsClickables = PickerUISettings.DEFAULT_ITEMS_CLICKABLES;

    private PickerUIItemClickListener mPickerUIListener;
    private PickerUIListView mPickerUIListView;
    private View mHiddenPanelPickerUI;
    private Context mContext;
    private List<String> items;
    private int position;
    private int position2;
    private PickerUIBlurHelper mPickerUIBlurHelper;
    private int backgroundColorPanel;
    private int colorLines;
    private int mColorTextCenterListView;
    private int mColorTextNoCenterListView;
    private PickerUISettings mPickerUISettings;
    private TextView mCancelButton;
    private TextView mSelectButton;
    private List<String> secondsItems;
    private PickerUIListView mPickerSecondUIListView;
    private PickerUISelectedItemsListener mItemsSelectedListenerPickerUI;
    private ViewGroup decorView;
    private View overlay;
    private View view;


    /**
     * Default constructor
     */
    public PickerUI(Context context) {
        super(context);
        mContext = context;
        if (isInEditMode()) {
            createEditModeView();
        } else {
            createView(null);
        }
    }

    /**
     * Default constructor
     */
    public PickerUI(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        if (isInEditMode()) {
            createEditModeView();
        } else {
            createView(attrs);
            getAttributes(attrs);
        }
    }

    /**
     * Default constructor
     */
    public PickerUI(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        if (isInEditMode()) {
            createEditModeView();
        } else {
            createView(attrs);
            getAttributes(attrs);
        }
    }

    private void onAttached(View view) {
        overlay = LayoutInflater.from(mContext).inflate(R.layout.overlay, null);
        decorView.addView(overlay);
        decorView.addView(view);
    }

    /**
     * This method inflates the panel to be visible from Preview Layout
     */
    private void createEditModeView() {
        LayoutInflater inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.pickerui, this, true);
    }


    private void createView(AttributeSet attrs) {
        LayoutInflater inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        decorView = (ViewGroup) ((Activity)mContext).getWindow().getDecorView().findViewById(android.R.id.content);
        view = inflater.inflate(R.layout.pickerui, decorView, false);
        view.setLayoutParams( new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, (int) mContext.getResources().getDimension(R.dimen.height_hidden_panel_pickerui), Gravity.BOTTOM
        ));
        mHiddenPanelPickerUI = view.findViewById(R.id.hidden_panel);
        mPickerUIListView = (PickerUIListView) view.findViewById(R.id.picker_ui_listview);
        mPickerSecondUIListView = (PickerUIListView) view.findViewById(R.id.picker_ui_listview2);
        mSelectButton = (TextView) view.findViewById(R.id.picker_ui_controls_select);
        mCancelButton = (TextView) view.findViewById(R.id.picker_ui_controls_cancel);
        mCancelButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                hidePanelPickerUI();
            }
        });
        mSelectButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                int position = 0;
                String secondItems = "";
                if(secondsItems != null) {
                    position = mPickerSecondUIListView.getItemInListCenter();
                    secondItems = secondsItems.get(position);
                }
                mItemsSelectedListenerPickerUI.onItemsSelectedItemPickerUI(mPickerUIListView.getItemInListCenter() ,items.get(mPickerUIListView.getItemInListCenter()),
                        position,secondItems);
                //mPickerUIListView.selectListItem(mPickerUIListView.getItemInListCenter(), true);
                //mPickerSecondUIListView.selectListItem(mPickerSecondUIListView.getItemInListCenter(), true);
                hidePanelPickerUI();
            }
        });
        setItemsClickables(itemsClickables);
        mPickerUIBlurHelper = new PickerUIBlurHelper(mContext, attrs);
        mPickerUIBlurHelper.setBlurFinishedListener(this);

    }

    /**
     * Retrieve styles attributes
     */
    private void getAttributes(AttributeSet attrs) {
        TypedArray typedArray = mContext.obtainStyledAttributes(attrs, R.styleable.PickerUI, 0, 0);

        if (typedArray != null) {
            try {

                autoDismiss = typedArray
                        .getBoolean(R.styleable.PickerUI_p_autoDismiss,
                                PickerUISettings.DEFAULT_AUTO_DISMISS);
                itemsClickables = typedArray
                        .getBoolean(R.styleable.PickerUI_p_itemsClickables,
                                PickerUISettings.DEFAULT_ITEMS_CLICKABLES);
                backgroundColorPanel = typedArray.getColor(R.styleable.PickerUI_p_backgroundColor,
                        getResources().getColor(R.color.background_panel_pickerui));
                colorLines = typedArray.getColor(R.styleable.PickerUI_p_linesCenterColor,
                        getResources().getColor(R.color.lines_panel_pickerui));
                mColorTextCenterListView = typedArray
                        .getColor(R.styleable.PickerUI_p_textCenterColor,
                                getResources().getColor(R.color.text_center_pickerui));
                mColorTextNoCenterListView = typedArray
                        .getColor(R.styleable.PickerUI_p_textNoCenterColor,
                                getResources().getColor(R.color.text_no_center_pickerui));

                int idItems;
                idItems = typedArray.getResourceId(R.styleable.PickerUI_p_entries, -1);
                if (idItems != -1) {
                    setItems(mContext, Arrays.asList(getResources().getStringArray(idItems)));
                }

            } catch (Exception e) {
                Log.e(LOG_TAG, "Error while creating the view PickerUI: ", e);
            } finally {
                typedArray.recycle();
            }
        }
    }

    /**
     * Slide the panel depending on the current state.
     * If slide up, the position is the half of the elements.
     */
    public void slide() {
        slide(position);
    }

    /**
     * Slide the panel depending depending on the current state.
     * If slide up, the position is the value selected.
     *
     * @param position the position to set in the center of the panel.
     */
    public void slide(final int position) {
        if (!isPanelShown()) {
            slideUp(position);
        } else {
            // Hide the Panel
            hidePanelPickerUI();
        }
    }

    /**
     * Slide the panel to the desired direction.
     *
     * @param slide the movement of the slide. See {@link PickerUI.SLIDE}
     */
    public void slide(SLIDE slide) {
        if (slide == SLIDE.UP) {
            if (!isPanelShown()) {
                slideUp(position);
            }
        } else {
            // Hide the Panel
            hidePanelPickerUI();
        }
    }

    /**
     * Show the panel to the position selected.
     *
     * @param position the position to set in the center of the panel.
     */
    private void slideUp(int position) {
        //Render to do the blur effect
        this.position = position;
        this.position2 = position;
        mPickerUIBlurHelper.render();
        onAttached(view);
    }

    /**
     * Hide the panel and clear blur image.
     */
    private void hidePanelPickerUI() {
        Animation bottomDown = AnimationUtils
                .loadAnimation(mContext, R.anim.picker_panel_bottom_down);
        mHiddenPanelPickerUI.startAnimation(bottomDown);

        bottomDown.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                decorView.removeView(view);
                decorView.removeView(overlay);

                // Hide the panel
                mHiddenPanelPickerUI.setVisibility(View.GONE);

                // Clear blur image.
                mPickerUIBlurHelper.handleRecycle();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

    }

    /**
     * Method to set if hide the panel when you click an item
     *
     * @param autoDismiss the behaviour selected to hide the panel
     */
    public void setAutoDismiss(boolean autoDismiss) {
        this.autoDismiss = autoDismiss;
    }

    /**
     * Sets the background color for the panel.
     *
     * @param color the color of the background
     */
    public void setBackgroundColorPanel(int color) {
        backgroundColorPanel = color;
    }

    /**
     * Sets the color of the lines of the center
     *
     * @param color the color of the lines
     */
    public void setLinesColor(int color) {
        colorLines = color;
    }

    /**
     * Method to enable the click of items
     *
     * @param itemsClickables the behaviour selected for items
     */
    public void setItemsClickables(boolean itemsClickables) {
        this.itemsClickables = itemsClickables;
        if (mPickerUIListView != null && mPickerUIListView.getPickerUIAdapter() != null) {
            mPickerUIListView.getPickerUIAdapter().setItemsClickables(itemsClickables);
        }
        if (mPickerSecondUIListView != null && mPickerSecondUIListView.getPickerUIAdapter() != null) {
            mPickerSecondUIListView.getPickerUIAdapter().setItemsClickables(itemsClickables);
        }
    }

    private void setTextColorsListView() {
        setColorTextCenter(mColorTextCenterListView);
        setColorTextNoCenter(mColorTextNoCenterListView);
    }

    public boolean isPanelShown() {
        return mHiddenPanelPickerUI.getVisibility() == View.VISIBLE;
    }

    /**
     * Method to set items to show in panel.
     * In this method, by default, the 'which' is 0 and the position is the half of the elements.
     *
     * @param items elements to show in panel
     */
    public void setItems(Context context, List<String> items) {
        if (items != null) {
            setItems(context, items, 0, items.size() / 2);
        }
    }

    /**
     * Method to set items to show in panel.
     * In this method, by default, the 'which' is 0 and the position is the half of the elements.
     *
     * @param items elements to show in panel
     */
    public void setSecondaryItems(Context context, List<String> items) {
        if (items != null) {
            mPickerSecondUIListView.setVisibility(VISIBLE);
            setSecondaryItems(context, items, 0, items.size() / 2);
        }
    }

    /**
     * Method to set items to show in panel.
     *
     * @param context  {@link PickerUIListView} needs a context
     * @param items    elements to show in panel
     * @param which    id of the element has been clicked
     * @param position the position to set in the center of the panel.
     */
    public void setItems(Context context, List<String> items, int which, int position) {
        if (items != null) {
            this.items = items;
            mPickerUIListView.setItems(context, items, which, position, itemsClickables);
            setTextColorsListView();
        }
    }

    /**
     * Method to set items to show in panel.
     *
     * @param context  {@link PickerUIListView} needs a context
     * @param items    elements to show in panel
     * @param which    id of the element has been clicked
     * @param position the position to set in the center of the panel.
     */
    public void setSecondaryItems(Context context, List<String> items, int which, int position) {
        if (items != null) {
            this.secondsItems = items;
            mPickerSecondUIListView.setItems(context, items, which, position, itemsClickables);
            setTextColorsListView();
        }
    }

    /**
     * This method will be run after you have the bitmap with the blur effect done (or not).
     */
    @Override
    public void onBlurFinished(Bitmap bitmapWithBlur) {

        if (bitmapWithBlur != null) {
            mPickerUIBlurHelper.showBlurImage(bitmapWithBlur);
        }

        // Show the panel
        showPanelPickerUI();
    }

    /**
     * Method to set the use of blur effect
     *
     * @param useBlur if want to use blur
     */
    public void setUseBlur(boolean useBlur) {
        if (mPickerUIBlurHelper != null) {
            mPickerUIBlurHelper.setUseBlur(useBlur);
        }
    }

    /**
     * Method to set the use of renderScript algorithm
     *
     * @param useRenderScript if want to use renderScript algorithm
     */
    public void setUseRenderScript(boolean useRenderScript) {
        if (mPickerUIBlurHelper != null) {
            mPickerUIBlurHelper.setUseRenderScript(useRenderScript);
        }
    }

    /**
     * Apply custom down scale factor
     * <p>
     * By default down scale factor is set to {@link PickerUIBlur#MIN_DOWNSCALE}
     *
     * @param downScaleFactor Factor customized down scale factor, must be at least 1.0
     */
    public void setDownScaleFactor(float downScaleFactor) {
        if (mPickerUIBlurHelper != null) {
            mPickerUIBlurHelper.setDownScaleFactor(downScaleFactor);
        }
    }

    /**
     * Select your preferred blur radius to apply
     * <p>
     * By default blur radius is set to {@link PickerUIBlur#MIN_BLUR_RADIUS}
     *
     * @param radius The radius to blur the image, radius must be at least 1
     */
    public void setBlurRadius(int radius) {
        if (mPickerUIBlurHelper != null) {
            mPickerUIBlurHelper.setBlurRadius(radius);
        }
    }

    /**
     * Select the color filter to the blur effect
     *
     * @param filterColor The color to overlay
     */
    public void setFilterColor(int filterColor) {
        if (mPickerUIBlurHelper != null) {
            mPickerUIBlurHelper.setFilterColor(filterColor);
        }
    }

    /**
     * Sets the text color for the item of the center.
     *
     * @param color the color of the text
     */
    public void setColorTextCenter(int color) {
        if (mPickerUIListView != null && mPickerUIListView.getPickerUIAdapter() != null) {

            int newColor;
            try {
                newColor = getResources().getColor(color);
            } catch (Resources.NotFoundException e) {
                newColor = color;
            }
            mColorTextCenterListView = newColor;
            mPickerUIListView.getPickerUIAdapter().setColorTextCenter(newColor);
        }
        if (mPickerSecondUIListView != null && mPickerSecondUIListView.getPickerUIAdapter() != null) {

            int newColor;
            try {
                newColor = getResources().getColor(color);
            } catch (Resources.NotFoundException e) {
                newColor = color;
            }
            mColorTextCenterListView = newColor;
            mPickerSecondUIListView.getPickerUIAdapter().setColorTextCenter(newColor);
        }
    }

    /**
     * Sets the text color for the items which aren't in the center.
     *
     * @param color the color of the text
     */
    public void setColorTextNoCenter(int color) {
        if (mPickerUIListView != null && mPickerUIListView.getPickerUIAdapter() != null) {
            int newColor;
            try {
                newColor = getResources().getColor(color);
            } catch (Resources.NotFoundException e) {
                newColor = color;
            }
            mColorTextNoCenterListView = newColor;
            mPickerUIListView.getPickerUIAdapter().setColorTextNoCenter(newColor);
        }
        if (mPickerSecondUIListView != null && mPickerSecondUIListView.getPickerUIAdapter() != null) {
            int newColor;
            try {
                newColor = getResources().getColor(color);
            } catch (Resources.NotFoundException e) {
                newColor = color;
            }
            mColorTextNoCenterListView = newColor;
            mPickerSecondUIListView.getPickerUIAdapter().setColorTextNoCenter(newColor);
        }
    }

    /**
     * Method to slide up the panel. Panel displays with an animation, and when it starts, the item
     * of the center is
     * selected.
     */
    private void showPanelPickerUI() {
        mHiddenPanelPickerUI.setVisibility(View.VISIBLE);
        setBackgroundPanel();
        setBackgroundLines();
        Animation bottomUp = AnimationUtils.loadAnimation(mContext, R.anim.picker_panel_bottom_up);
        mHiddenPanelPickerUI.startAnimation(bottomUp);
        bottomUp.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                if (mPickerUIListView != null && mPickerUIListView.getPickerUIAdapter() != null) {
                    mPickerUIListView.getPickerUIAdapter().handleSelectEvent(position + 2);
                    mPickerUIListView.setSelection(position);
                }
                if (mPickerSecondUIListView != null && mPickerSecondUIListView.getPickerUIAdapter() != null) {
                    mPickerSecondUIListView.getPickerUIAdapter().handleSelectEvent(position + 2);
                    mPickerSecondUIListView.setSelection(position2);
                }
            }

            @Override
            public void onAnimationEnd(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
    }


    private void setBackgroundPanel() {
        int color;
        try {
            color = getResources().getColor(backgroundColorPanel);
        } catch (Resources.NotFoundException e) {
            color = backgroundColorPanel;
        }
        mHiddenPanelPickerUI.setBackgroundColor(color);
    }

    private void setBackgroundLines() {
        int color;
        try {
            color = getResources().getColor(colorLines);
        } catch (Resources.NotFoundException e) {
            color = colorLines;
        }

        //Top line
        mHiddenPanelPickerUI.findViewById(R.id.picker_line_top).setBackgroundColor(color);

        //Bottom line
        mHiddenPanelPickerUI.findViewById(R.id.picker_line_bottom).setBackgroundColor(color);
    }

    /**
     * Set a callback listener for the item click.
     *
     * @param listener Callback instance.
     */
    public void setOnClickItemPickerUIListener(final PickerUIItemClickListener listener) {
        this.mPickerUIListener = listener;

        mPickerUIListView.setOnClickItemPickerUIListener(
                new PickerUIListView.PickerUIItemClickListener() {
                    @Override
                    public void onItemClickItemPickerUI(int which, int position,
                                                        String valueResult) {
                        if (autoDismiss) {
                            slide(position);
                        }

                        if (mPickerUIListener == null) {
                            throw new IllegalStateException(
                                    "You must assign a valid PickerUI.PickerUIItemClickListener first!");
                        }
                        mPickerUIListener.onItemClickPickerUI(which, position, valueResult);
                    }
                });

        if (mPickerSecondUIListView != null)
            mPickerSecondUIListView.setOnClickItemPickerUIListener(
                    new PickerUIListView.PickerUIItemClickListener() {
                        @Override
                        public void onItemClickItemPickerUI(int which, int position,
                                                            String valueResult) {
                            if (autoDismiss) {
                                slide(position);
                            }

                            if (mPickerUIListener == null) {
                                throw new IllegalStateException(
                                        "You must assign a valid PickerUI.PickerUIItemClickListener first!");
                            }
                            mPickerUIListener.onItemClickPickerUI(which, position, valueResult);
                        }
                    });
    }

    /**
     * This method sets the desired functionalities of panel to make easy.
     *
     * @param pickerUISettings Object with all functionalities to make easy.
     */
    public void setSettings(PickerUISettings pickerUISettings) {
        mPickerUISettings = pickerUISettings;
        setColorTextCenter(pickerUISettings.getColorTextCenter());
        setColorTextNoCenter(pickerUISettings.getColorTextNoCenter());
        setItems(mContext, pickerUISettings.getItems());
        setBackgroundColorPanel(pickerUISettings.getBackgroundColor());
        setLinesColor(pickerUISettings.getLinesColor());
        setItemsClickables(pickerUISettings.areItemsClickables());
        setUseBlur(pickerUISettings.isUseBlur());
        setUseRenderScript(pickerUISettings.isUseBlurRenderscript());
        setAutoDismiss(pickerUISettings.isAutoDismiss());
        setBlurRadius(pickerUISettings.getBlurRadius());
        setDownScaleFactor(pickerUISettings.getBlurDownScaleFactor());
        setFilterColor(pickerUISettings.getBlurFilterColor());
    }

    /**
     * Save the state of the panel when orientation screen changed.
     */
    @Override
    public Parcelable onSaveInstanceState() {

        Bundle bundle = new Bundle();
        bundle.putParcelable("instanceState", super.onSaveInstanceState());
        bundle.putParcelable("stateSettings", mPickerUISettings);
        //save everything
        bundle.putBoolean("stateIsPanelShown", isPanelShown());
        bundle.putInt("statePosition", mPickerUIListView.getItemInListCenter());
        bundle.putInt("statePosition2", mPickerSecondUIListView.getItemInListCenter());
        return bundle;
    }

    /**
     * Retrieve the state of the panel when orientation screen changed.
     */
    @Override
    public void onRestoreInstanceState(Parcelable state) {

        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            //load everything
            PickerUISettings pickerUISettings = bundle.getParcelable("stateSettings");
            if (pickerUISettings != null) {
                setSettings(pickerUISettings);
            }

            boolean stateIsPanelShown = bundle.getBoolean("stateIsPanelShown");
            if (stateIsPanelShown) {

                final int statePosition = bundle.getInt("statePosition");

                ViewTreeObserver observer = getViewTreeObserver();
                observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {

                        slideUp(statePosition);

                        if (android.os.Build.VERSION.SDK_INT
                                >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
                            getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        } else {
                            //noinspection deprecation
                            getViewTreeObserver().removeGlobalOnLayoutListener(this);
                        }

                    }
                });

            }
            state = bundle.getParcelable("instanceState");
        }
        super.onRestoreInstanceState(state);
    }

    public enum SLIDE {
        UP,
        DOWN
    }

    /**
     * Interface for a callback when the item has been clicked.
     */
    public interface PickerUIItemClickListener {

        /**
         * Callback when the item has been clicked.
         *
         * @param which       id of the element has been clicked
         * @param position    Position of the current item.
         * @param valueResult Value of text of the current item.
         */
        public void onItemClickPickerUI(int which, int position, String valueResult);
    }

    public void setOnSelectedItemsPickerUIListener(PickerUISelectedItemsListener listener) {
        this.mItemsSelectedListenerPickerUI = listener;
    }

    public interface PickerUISelectedItemsListener {

        void onItemsSelectedItemPickerUI(int position, String valueResult,int position2, String valueResult2);
    }

}
