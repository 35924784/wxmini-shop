package com.mingyue.common;

import java.util.Collection;

public class ValidateUtils{

    public static boolean isEmptyString(String str)
    {
        if (str == null || str.length() == 0)
        {
            return true;
        }
        return false;
    }

    public static boolean isNotEmptyString(String str)
    {
        if (str != null && str.length() > 0)
        {
            return true;
        }
        return false;
    }

    public static boolean isNotEmptyCollection(Collection<?> collection)
    {
        if (collection != null && collection.size() > 0)
        {
            return true;
        }

        return false;
    }
}