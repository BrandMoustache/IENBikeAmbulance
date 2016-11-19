package mainbrain.tech.ienbikeambulance.design;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;

//Custom Progress Dialog
public class CustomProgressDialog extends ProgressDialog
{
	private Context ctx;

	public CustomProgressDialog(Context context, int theme)
	{
        super(context,theme);
		ctx=context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        View view = this.findViewById(android.R.id.message);
        View button = this.findViewById(android.R.id.button1);
        if (view != null) 
        {
            // Setting up font to progress text
           new Sansation().overrideFonts(ctx, view);
        }
        if (button != null) 
        {
            // Setting up font to progress text
           new Sansation().overrideFonts(ctx, button);
        }
    }
}
