# ResourceTools

This is a dimen conversion tool, which can convert the px, 
sp and dp in your xml file to the default size of different resolutions, 
of course you can also customize the density and name


Example:


    <TextView
        android:id="@+id/vo_title_tv"
        android:layout_width="1px"
        android:layout_height="2dp"
        android:textSize="3sp"
        android:lines="2" />
        
        

Will be generated under the dimen.xml file in the default directory:

    <dimen name="dimen_0_33dp">0.33dp</dimen>
    <dimen name="dimen_2dp">2dp</dimen>
    <dimen name="dimen_3sp">3sp</dimen>
