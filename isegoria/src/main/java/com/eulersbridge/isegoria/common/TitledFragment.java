package com.eulersbridge.isegoria.common;

import android.content.Context;
import android.support.annotation.Nullable;

public interface TitledFragment {
    @Nullable String getTitle(Context context);
}