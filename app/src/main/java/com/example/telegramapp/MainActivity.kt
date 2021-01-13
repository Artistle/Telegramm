package com.example.telegramapp

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.widget.Toolbar
import com.example.telegramapp.activityes.RegisterActivity
import com.example.telegramapp.databinding.ActivityMainBinding
import com.example.telegramapp.models.User
import com.example.telegramapp.ui.fragments.ChatsFragment
import com.example.telegramapp.ui.objects.AppDrawer
import com.example.telegramapp.utilits.*
import com.theartofdev.edmodo.cropper.CropImage

class MainActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivityMainBinding
    lateinit var appDrawer:AppDrawer
    private lateinit var mToolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
    }

    override fun onStart() {
        super.onStart()
        APP_ACTIVITY = this
        initFields()
        initFunction()
    }

    private fun initFunction() {

        if(AUTH.currentUser != null){
            setSupportActionBar(mToolbar)
            appDrawer.create()
            replaceFragment(ChatsFragment(),false)
        }else{
            replaceActivity(RegisterActivity())
        }


//        createHeader()
//        createDrawer()
    }



    private fun initFields() {
        mToolbar = mBinding.mainToolbar
        appDrawer = AppDrawer(this,mToolbar)
        initFirebase()
        initUser()
    }

    private fun initUser() {
        REF_DATABASE_ROOT.child(NODE_USERS).child(CURRENT_UID)
            .addListenerForSingleValueEvent(AppValueEventListener{
                USER = it.getValue(USER::class.java) ?: User()
            })
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE
            && resultCode == Activity.RESULT_OK
            && data != null){
            val uri = CropImage.getActivityResult(data).uri
            val path = REF_STORAGE_ROOT
                .child(FOLDER_PROFILE_IMAGE)
                .child(CURRENT_UID)
            path.putFile(uri).addOnCompleteListener {task ->
                if(task.isSuccessful){
                    path.downloadUrl.addOnCompleteListener {task2 ->
                        if(task2.isSuccessful){
                            val photoUrl = task2.result.toString()
                            REF_DATABASE_ROOT
                                .child(NODE_USERS)
                                .child(CURRENT_UID)
                                .child(CHILD_PHOTO_URL)
                                .setValue(photoUrl)
                                .addOnCompleteListener {
                                    if(it.isSuccessful){
                                      showToast(getString(R.string.toast_data_update))
                                        USER.photoUrl = photoUrl
                                    }
                                }
                        }
                    }
                }
            }
        }
    }


    fun hideKeyboard(){
        val imm:InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(window.decorView.windowToken,0)
    }
}