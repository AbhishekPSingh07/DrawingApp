package com.example.kidsdrawingapp

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View

class DrawingView(context: Context, attrs : AttributeSet) : View(context,attrs) {
    private var mDrawPath : CustomPath? = null
    private var mCanvasBitmap : Bitmap? = null
    private var mDrawPaint : Paint? = null
    private var mCanvasPaint : Paint? = null
    private var mBrushSize :Float =0.toFloat()
    private var color = Color.BLACK
    private var canvas : Canvas? = null
    private val mPaths = ArrayList<CustomPath>()
    private val mUndo = ArrayList<CustomPath>()

    init {
        setupDrawing()
    }

    private fun setupDrawing() {
        mDrawPaint = Paint()
        mDrawPath = CustomPath(color,mBrushSize)
        mDrawPaint!!.color = Color.BLACK
        mDrawPaint!!.style = Paint.Style.STROKE
        mDrawPaint!!.strokeJoin = Paint.Join.ROUND
        mDrawPaint!!.strokeCap = Paint.Cap.ROUND
        mCanvasPaint = Paint(Paint.DITHER_FLAG)

    }
    fun clickOnUndo(){
        if(mPaths.size > 0){
            //.remove returns the old path that is it would return the path drawn an index before the current one.
            mUndo.add(mPaths.removeAt(mPaths.size - 1))
            //it will call onDraw again that is it would delete the upper remove one
            invalidate()
        }
    }


    fun setColor( newColor: String){
        color = Color.parseColor(newColor)
        mDrawPaint!!.color = color
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawBitmap(mCanvasBitmap!!,0f,0f,mCanvasPaint)
         for(path in mPaths){
             mDrawPaint!!.strokeWidth = path.brushThickness
             mDrawPaint!!.color = path.colour
             canvas.drawPath(path,mDrawPaint!!)
         }
        if(!mDrawPath!!.isEmpty){
            mDrawPaint!!.strokeWidth = mDrawPath!!.brushThickness

            mDrawPaint!!.color = mDrawPath!!.colour
            canvas.drawPath(mDrawPath!!,mDrawPaint!!)
        }

    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        var touchx = event?.x
        var touchy = event?.y

        when(event?.action){
            MotionEvent.ACTION_DOWN ->{
                mDrawPath!!.colour = color
                mDrawPath!!.brushThickness = mBrushSize

                mDrawPath!!.reset()
                mDrawPath!!.moveTo(touchx!!,touchy!!)
            }
            MotionEvent.ACTION_MOVE ->{
                mDrawPath!!.lineTo(touchx!!,touchy!!)
            }
            MotionEvent.ACTION_UP ->{
                mPaths.add(mDrawPath!!)
                mDrawPath = CustomPath(color,mBrushSize)

            }
            else -> return false
        }
        invalidate();

        return true
    }
    fun setSizeForBrush(newSize :Float){
        mBrushSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
            newSize,resources.displayMetrics)
        mDrawPaint!!.strokeWidth = mBrushSize
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mCanvasBitmap = Bitmap.createBitmap(w , h, Bitmap.Config.ARGB_8888)
        canvas = Canvas(mCanvasBitmap!!)
    }

    internal inner class CustomPath(var colour:Int,var brushThickness: Float ) : Path() {


    }
}