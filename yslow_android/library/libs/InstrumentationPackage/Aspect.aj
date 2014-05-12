
package com.example.testfragment;


import android.support.v4.app.FragmentManager;

aspect onCreate
{ 
	pointcut captureOnCreate() : (execution(* onCreate(*))&& within(com.example.testfragment..*));
	after(): captureOnCreate() 
	{ 
			FragmentManager.enableDebugLogging(true);
	}
}