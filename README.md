# android_app_actualization_example
The simple way to check for update and update your android app without Google Play Market 

You may use this code if you need to organize automatic actualization from your own server in you android app

Also you need add some permissions in your manifes, like :

    <uses-permission android:name="android.permission.REQUEST_INSTALL_PACKAGES"/>
    <uses-permission android:name="android.permission.REQUEST_DELETE_PACKAGES"/>
    <uses-permission android:name="android.permission.REQUEST_CLEAR_APP_CACHE"/>
    <uses-permission android:name="android.permission.REQUEST_MOUNT_UNMOUNT_FILESYSTEMS"/>
    
and internet ofcourse :

    <uses-permission android:name="android.permission.INTERNET" />
