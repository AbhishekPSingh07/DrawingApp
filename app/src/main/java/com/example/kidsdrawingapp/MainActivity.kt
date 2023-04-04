package com.example.kidsdrawingapp

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.media.MediaScannerConnection
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.ActivityChooserView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.get
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.dialog_brush_size.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception
import java.util.jar.Manifest

class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mImageCurrentImageBtn = linear_layout[11] as ImageButton
        mImageCurrentImageBtn!!.setImageDrawable(
            ContextCompat.getDrawable(this,R.drawable.pallet_pressed)
        )
        drawing_view.setSizeForBrush(5.toFloat())
        ib_brush.setOnClickListener{
            brushSizeChoose()
        }
        //Setting up Undo Button
        Undo.setOnClickListener {
            // calling the undo function from drawing view
            drawing_view.clickOnUndo()
        }
        // Setting the Image Button
        imageButton.setOnClickListener{
            if(isReadStorageAllowed()){
                val pickPhotoIntent = Intent(Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(pickPhotoIntent, GALLARY)
            }else{
                requestStoragePermission()
            }
        }

        save.setOnClickListener{
            if(isReadStorageAllowed()){
                bitmapAsyncTask(getBitmapFromView(fl_layout)).execute()
            }else{
                requestStoragePermission()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == GALLARY){
            try {
                if(data!!.data != null){
                    iv_imgView.visibility = View.VISIBLE
                    iv_imgView.setImageURI(data.data)
                }else{
                    Toast.makeText(this,"ERROR Parsing Image",
                    Toast.LENGTH_SHORT).show()
                }
            }catch (e:Exception){
                e.printStackTrace()
            }
        }
    }
    //Creating a function to extract bitmap out of the Drawing view for saving purpose
    private fun getBitmapFromView(view: View):Bitmap{
        //ARGB_8888 sets the color to be seen and stuff
        val returnedBitmap = Bitmap.createBitmap(view.width,view.height,Bitmap.Config.ARGB_8888)
        //For Including the background
        val canvas = Canvas(returnedBitmap)
        val bgDrawable = view.background
        if(bgDrawable != null){
            bgDrawable.draw(canvas)
        }else{
            canvas.drawColor(Color.WHITE)
        }
        //finalise the thing
        view.draw(canvas)

        return returnedBitmap
    }

    private inner class bitmapAsyncTask(val mBitmap: Bitmap):
        AsyncTask<Any, Void, String>(){

        override fun onPreExecute() {
            super.onPreExecute()
            showDialog()
        }

        private lateinit var mProgressDialog: Dialog
        override fun doInBackground(vararg params: Any?): String {
            var result = " "
            if(mBitmap != null){
                try {
                    //byteArrayOutputS
                    val bytes = ByteArrayOutputStream()
                    mBitmap.compress(Bitmap.CompressFormat.PNG,90,bytes)
                    val f = File(externalCacheDir!!.absoluteFile.toString() + File.separator
                    + "KidsDrawingApp_"  + System.currentTimeMillis()/1000 + ".png")

                    val fos = FileOutputStream(f)
                    fos.write(bytes.toByteArray())
                    fos.close()
                    result = f.absolutePath
                }catch(e : Exception) {
                    result = ""
                    e.printStackTrace()
                }
            }
            return result
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            cancelProgressdialog()
            if(!result!!.isEmpty()){
                //MainActivity used as were are in context of Async Task and not in Main Activity
                Toast.makeText(this@MainActivity,"File Saved Successfully",
                    Toast.LENGTH_SHORT).show()
            }else{
                Toast.makeText(this@MainActivity,"There was some Problem in saving the File",
                    Toast.LENGTH_SHORT).show()
            }
            MediaScannerConnection.scanFile(this@MainActivity, arrayOf(result),null,){
                path, uri -> val shareIntent= Intent()
                shareIntent.action = Intent.ACTION_SEND
                shareIntent.putExtra(Intent.EXTRA_STREAM,uri)
                shareIntent.type = "image/png"

                startActivity(
                    Intent.createChooser(
                        shareIntent,"share"
                    )
                )
            }

        }

        private fun showDialog(){
            mProgressDialog = Dialog(this@MainActivity)
            mProgressDialog.setContentView(R.layout.progress_bar)
            mProgressDialog.show()
        }

        private fun cancelProgressdialog(){
            mProgressDialog.dismiss()
        }

    }


    // Requesting the Permission For accessing the Storage Permission.
    private fun requestStoragePermission(){
        if(ActivityCompat.shouldShowRequestPermissionRationale(this,
                arrayOf(
                    android.Manifest.permission.READ_EXTERNAL_STORAGE ,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                ).toString())){
            Toast.makeText(this,"NEED BACKGROUND IMAGE PERMISSION",Toast.LENGTH_SHORT).show()
        }
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            ), STORAGE_PERMISSION)
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == STORAGE_PERMISSION){
            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this,
                "PERMISSION GRANTED",
                Toast.LENGTH_SHORT).show()
            }else{
                Toast.makeText(this,
                    "PERMISSION Denied ",
                    Toast.LENGTH_SHORT).show()
            }
        }
    }

    // To Check if storage Access is Allowed in the settings too..
    private fun isReadStorageAllowed():Boolean{
        val result = ContextCompat.checkSelfPermission(this,
            android.Manifest.permission.READ_EXTERNAL_STORAGE)
        return result == PackageManager.PERMISSION_GRANTED


    }


    // Creating a Permission Code
    companion object{
        private const val STORAGE_PERMISSION = 1
        private const val GALLARY = 2
    }
    private var mImageCurrentImageBtn : ImageButton? = null

    private fun brushSizeChoose(){
        val brushDialog = Dialog(this)
        brushDialog.setContentView(R.layout.dialog_brush_size)
        brushDialog.setTitle("Brush Size: ")
        val verySmallBtn = brushDialog.ib_very_small
        verySmallBtn.setOnClickListener{
            drawing_view.setSizeForBrush(5.toFloat())
            brushDialog.dismiss()
        }
        val Small_Btn = brushDialog.ib_small
        Small_Btn.setOnClickListener{
            drawing_view.setSizeForBrush(10.toFloat())
            brushDialog.dismiss()
        }
        val Medium_Btn = brushDialog.ib_medium
        Medium_Btn.setOnClickListener{
            drawing_view.setSizeForBrush(15.toFloat())
            brushDialog.dismiss()
        }
        val large_Btn = brushDialog.ib_large
        large_Btn.setOnClickListener{
            drawing_view.setSizeForBrush(20.toFloat())
            brushDialog.dismiss()
        }
        val veryLargeBtn = brushDialog.ib_very_Large
        veryLargeBtn.setOnClickListener{
            drawing_view.setSizeForBrush(25.toFloat())
            brushDialog.dismiss()
        }
        brushDialog.show()
    }
    fun paintClicked(view: View){
        if(view !== mImageCurrentImageBtn){
            val imageButton = view as ImageButton

            val colorTag = imageButton.tag.toString()
            drawing_view.setColor(colorTag)
            imageButton.setImageDrawable(
                ContextCompat.getDrawable(this,R.drawable.pallet_pressed))
            mImageCurrentImageBtn!!.setImageDrawable(
                ContextCompat.getDrawable(this,R.drawable.pallet))
            mImageCurrentImageBtn = view
        }
    }

}