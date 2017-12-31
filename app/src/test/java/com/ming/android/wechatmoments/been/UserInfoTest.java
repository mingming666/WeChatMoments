package com.ming.android.wechatmoments.been;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * Created by MYNOTEBOOK on 2017/12/31.
 */
public class UserInfoTest {

    private String mUserName;
    private UserInfo mUserInfo;

    @Before
    public void setUp()throws Exception {
        mUserName = "username";
        mUserInfo = new UserInfo();
        mUserInfo.setUsername(mUserName);

    }
    @Test
    public void getUsername() throws Exception {
        assertThat(mUserInfo.getUsername(), is(mUserName));
    }


}