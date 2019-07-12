package org.ilri.eweigh.utils;

import org.ilri.eweigh.BuildConfig;

public class URL {

    private static String BASE_URL = BuildConfig.DEBUG ?
            "http://192.168.1.102/Code/ilri/eweigh-api/" :
            "http://test.tickos.co.ke/eweigh/";

    private static final String BASE_API_URL = BASE_URL + "api/v1/";
    private static final String RESOURCE_URL = BASE_URL + "content/uploads/";

    // Resource URL
    public static final String AvatarPhotoUrl = RESOURCE_URL + "avatars/";

    // Auth
    public static final String Register = BASE_API_URL + "register";
    public static final String Login = BASE_API_URL + "login";
    public static final String UpdateAccount = BASE_API_URL + "user/update";
    public static final String Verify = BASE_API_URL + "account/verify";

    // HG/LW
    public static final String GetLiveWeight = BASE_API_URL + "lw";
    public static final String Cattle = BASE_API_URL + "cattle";
    public static final String RegisterCattle = BASE_API_URL + "cattle/register";

    // Reports
    public static final String Submissions = BASE_API_URL + "report/submissions";
}
