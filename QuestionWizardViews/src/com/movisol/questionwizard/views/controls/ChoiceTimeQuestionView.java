package com.movisol.questionwizard.views.controls;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.movisol.questionwizard.applicationcontroller.ApplicationController;
import com.movisol.questionwizard.entities.Choice;
import com.movisol.questionwizard.entities.ChoiceImageQuestion;
import com.movisol.questionwizard.entities.ChoiceQuestion;
import com.movisol.questionwizard.interfaces.ScreenViewable;
import com.movisol.questionwizard.views.R;
import com.movisol.questionwizard.views.listeners.CompositeListener;
import com.movisol.questionwizard.views.listeners.MoveForwardEvent;
import com.movisol.questionwizard.views.listeners.MoveForwardListener;
import com.movisol.tools.HelperUtils;


public class ChoiceTimeQuestionView extends LinearLayout implements OnClickListener, ScreenViewable {
	
	private static final String CHOICEA = "choicea";
	private static final String CHOICEB = "choiceb";
	private static final String CHOICEC = "choicec";
	private static final String CHOICED = "choiced";
	private static final String CHOICEE = "choicee";
	private static final String CHOICEF = "choicef";
	private static final String CHOICEG = "choiceg";
	private String[] choicesImagesList = {CHOICEA,CHOICEB,CHOICEC,CHOICED,CHOICEE,CHOICEF,CHOICEG};
	
	private static final String CHOICEAS = "choiceaselected";
	private static final String CHOICEBS = "choicebselected";
	private static final String CHOICECS = "choicecselected";
	private static final String CHOICEDS = "choicedselected";
	private static final String CHOICEES = "choiceeselected";
	private static final String CHOICEFS = "choicefselected";
	private static final String CHOICEGS = "choicegselected";
	private String[] selectedChoicesImagesList = {CHOICEAS,CHOICEBS,CHOICECS,CHOICEDS,CHOICEES,CHOICEFS,CHOICEGS};
	
	private int MAX_TIME_BONUS;
	private ChoiceQuestion question;
	private  List<Choice> choiceList;

	private List<Button> buttonList;
	private ApplicationController ac = ApplicationController.getInstance();
	CompositeListener compositeListener;
	private CountDownTimer countDown;
	private long maxTime;
	private int remainingBonus;
	private TextView bonus;
	private long millisToFinish;
	
	private ProgressBar pbChoiceImage;
	protected List<MoveForwardListener> listeners = new ArrayList<MoveForwardListener>();
	private boolean canceled = false;
	private Context context; 

	public ChoiceTimeQuestionView(Context context) {
		super(context);
		String infService = Context.LAYOUT_INFLATER_SERVICE;
		LayoutInflater li = (LayoutInflater)getContext().getSystemService(infService);
		li.inflate(R.layout.choicetimequestion, this, true);
		setBackgroundResource(HelperUtils.getDrawableResource("containeropaco", context));
		getBackground().setDither(true);
		this.context = context;
	//	MAX_TIME_BONUS = Integer.parseInt(HelperUtils.getConfigParam("QWMaxTimeBonusPerQuestion", getContext()));
}
	public synchronized void addMoveForwardListener(MoveForwardListener listener){
		listeners.add(listener);
	}
	public synchronized void removeMoveForwardListener(MoveForwardListener listener){
		listeners.remove(listener);
	}
	
	public void fireTimeExpiredEvent(){
		MoveForwardEvent mfe = null;
		mfe = new MoveForwardEvent(this, "move_forward");
		Iterator<MoveForwardListener> it = listeners.listIterator();
		while (it.hasNext()){
			((ChoiceQuestion)ac.getActualQuestion()).setQuestionTip(ac.getLiteralsValueByKey("noAnswer"));
			//ac.set
			 it.next().onTimeExpired(mfe);
		}
	}
	@Override
	protected void onWindowVisibilityChanged(int visibility) {
		super.onWindowVisibilityChanged(visibility);
		switch(visibility)
		{
		case View.VISIBLE:
			if(pbChoiceImage.getVisibility() == View.VISIBLE)
			{Log.e("Inside Visible State","power key pressed");
				if(countDown == null)
					newCountDown();
				canceled = false;
				countDown.start();
			}
			
		break;
	
		case View.GONE:
			if(pbChoiceImage.getVisibility() == View.VISIBLE && countDown != null)
			{
				Log.e("Inside the GOne","power key pressed");
				countDown.cancel();
				canceled = true;
				countDown = null;
			}
		break;
		
	}

		/*switch(visibility)
		{
		case View.VISIBLE:
			if(countDown == null)
			{
				countDown = new CountDownTimer(millisToFinish, 1) {

				     public void onTick(long millisUntilFinished) {
				    	 millisToFinish = millisUntilFinished; 
				         float remainingProgress = (float)Math.floor((millisUntilFinished/(maxTime*1000f)*100));
				         remainingBonus = (int)(MAX_TIME_BONUS*remainingProgress/100f); 
				         
				         if(remainingBonus <= MAX_TIME_BONUS)
				        	 bonus.setText("BONUS: "+remainingBonus);
				     }

				     public void onFinish() {
				     }
				  };
			}
			countDown.start();
		break;
		
		case View.GONE:
			if(countDown != null)
			  countDown.cancel();
			countDown = null;
		break;
		}*/
	}

	public void cancelTimer() {
		if(countDown != null)
			countDown.cancel();
		canceled  = true;
		
	}
	@SuppressWarnings("rawtypes")
	public void initialize (CompositeListener cl)
	{
		buttonList = new ArrayList<Button>();
		choiceList = new ArrayList<Choice>();
		compositeListener = cl;
		
		//Gets the actual question from the Singleton Application Controller
		question = (ChoiceQuestion) ac.getActualQuestion();

		//Adds the listener of this class to ours composite listeners
		compositeListener.addListener(this);

    
		if (question != null)
		{
			
			if(question.getBackground() != null)
			{
				setBackgroundResource(HelperUtils.getDrawableResource(question.getBackground(), getContext()));
			}
			getBackground().setDither(true);
			//Adds the statement of the question to the current view
			TextView title = (TextView) findViewById(R.id.tvTimeTitle);
			title.setText(question.getTitle());
			
/*			bonus = (TextView) findViewById(R.id.tvBonus);
			bonus.setText("BONUS: "+MAX_TIME_BONUS);
			bonus.setTextColor(Color.RED);
			maxTime = (long)question.getAnswerTime();
			millisToFinish = maxTime*1000L+1500;
			
			countDown = new CountDownTimer(millisToFinish, 1) {

			     public void onTick(long millisUntilFinished) {
			    	 millisToFinish = millisUntilFinished;
			         float remainingProgress = (float)Math.floor((millisUntilFinished/(maxTime*1000f)*100));
			         remainingBonus = (int)(MAX_TIME_BONUS*remainingProgress/100f); 
			         
			         if(remainingBonus <= MAX_TIME_BONUS)
			        	 bonus.setText("BONUS: "+remainingBonus);
			     }*/
/*
			     public void onFinish() {
			    	 
//			    	 Message msg = new Message();
//			    	 Bundle data = new Bundle();
//			    	 data.putString("data", "finish");
//			    	 msg.setData(data);
//			    	 handler.sendMessage(msg);
			     }
			  };*/

			//Loads the predefined style for the title of the question
//			title.setTextAppearance(getContext(), HelperUtils.getStyleResource("questionTitle", getContext()));
			pbChoiceImage = (ProgressBar) findViewById(R.id.pbChoiceImage);
			
			if(question.getAnswerTime() != -1 && Boolean.parseBoolean(HelperUtils.getConfigParam("QWQuestionTimeEnabled", context)))
			{
				pbChoiceImage.setProgressDrawable(ac.getContext().getResources().getDrawable(HelperUtils.getDrawableResource("progressbar_color", ac.getContext())));
				//Convert to msecs
				pbChoiceImage.setMax(question.getAnswerTime()*1000);
				pbChoiceImage.setProgress(0);
				millisToFinish = question.getAnswerTime()*1000L;
			
				newCountDown();
			}
			else
				pbChoiceImage.setVisibility(View.GONE);	
			
			Iterator it = question.getChoices().iterator();
	
			int i = 0;
			Button choiceTmp = (Button) findViewById(R.id.btTimeChoice);
			LinearLayout choicesLayout = (LinearLayout) findViewById(R.id.choicesTimeLayout);
			LinearLayout choiceLayout = (LinearLayout) findViewById(R.id.choiceTimeLayout);
			while (it.hasNext()) 
			{
				choiceList.add((Choice) it.next());
				
				Button choiceBtn = new Button(getContext());
			    LinearLayout layout = new LinearLayout(getContext());
			    
				choiceBtn.setGravity(choiceTmp.getGravity());
				choiceBtn.setText(choiceList.get(choiceList.size()-1).getTitle());
				
				choiceBtn.setBackgroundResource(HelperUtils.getDrawableResource(choicesImagesList[i], getContext()));
				//Sets the predefined style for the Button
				choiceBtn.setTextAppearance(getContext(), HelperUtils.getStyleResource("choiceTitleNormal", getContext()));
				
				if(choiceBtn.getText().length() > 38)
				{
					//Sets the small style for the Button				
					choiceBtn.setTextAppearance(getContext(), HelperUtils.getStyleResource("choiceTitleSmall", getContext()));
				}
				
				
				if(question.isAnswered() && question.getSelectedValue().getTitle().equals(choiceBtn.getText()))
				{
					choiceBtn.setBackgroundResource(HelperUtils.getDrawableResource(selectedChoicesImagesList[i], getContext()));
					choiceBtn.setTextAppearance(getContext(), HelperUtils.getStyleResource("textChoiceSelected", getContext()));
				}
				
				choiceBtn.setPadding(choiceTmp.getPaddingLeft(), 0, choiceTmp.getPaddingRight(), 0);			
			
				buttonList.add(choiceBtn);
				layout.addView(choiceBtn, choiceLayout.getLayoutParams());
				layout.setPadding(choiceLayout.getPaddingLeft(), choiceLayout.getPaddingTop(), choiceLayout.getPaddingRight(), choiceLayout.getPaddingBottom());
				choicesLayout.addView(layout);
				i++;
			}
			
			choicesLayout.removeViewAt(0);
		}
}

	private void newCountDown() {
		countDown = new CountDownTimer(millisToFinish, 1) {

		     public void onTick(long millisUntilFinished) {
		    	 millisToFinish = millisUntilFinished; 
		    	 pbChoiceImage.setProgress((int)(question.getAnswerTime()*1000-millisUntilFinished));
		    
		    	 }

		     public void onFinish() {
		    	 if(!canceled)
		    		 fireTimeExpiredEvent();
		     }
		  };
		
	}
	public void setQuestion(ChoiceQuestion question) {
		this.question = question;
	}

	
	public ViewGroup getLayout() {
		return this;
	}



	public List<Choice> getChoiceList() {
		return choiceList;
	}


	public void setChoiceList(List<Choice> choiceList) {
		this.choiceList = choiceList;
	}


	public List<Button> getButtonList() {
		return buttonList;
	}


	public void setButtonList(List<Button> buttonList) {
		this.buttonList = buttonList;
	}


	@Override
	public void onClick(View v) {
		countDown.cancel();
//		((ChoiceQuestion)ac.getActualQuestion()).setRemainingBonus(remainingBonus);
		for(int i = 0; i < choiceList.size(); i++)
		{
			buttonList.get(i).setBackgroundResource(HelperUtils.getDrawableResource(choicesImagesList[i], getContext()));
			buttonList.get(i).setTextAppearance(getContext(), HelperUtils.getStyleResource("textChoice", getContext()));
			//Once we have selected a choice, we look for the one that was clicked	
			if( choiceList.get(i).getTitle().equals( ((Button)v).getText()))
			{
				ac.getActualQuestion().setAnswered(true);
				//Changes the background image of the button clicked
				buttonList.get(i).setBackgroundResource(HelperUtils.getDrawableResource(selectedChoicesImagesList[i], getContext()));
				buttonList.get(i).setTextAppearance(getContext(), HelperUtils.getStyleResource("textChoiceSelected", getContext()));
				//TODO Hacer un swtich para elegir que casting se ha de hacer a la pregunta
				((ChoiceQuestion)ac.getActualQuestion()).setSelectedValue(choiceList.get(i));
				//If our choice has tip, then set it up into the question

				if(choiceList.get(i).getTip() != null && choiceList.get(i).getTip().length() > 0)
					((ChoiceQuestion)ac.getActualQuestion()).setQuestionTip(choiceList.get(i).getTip());
			}
		}
		
	}

	@Override
	public void initializeControls() {
		// TODO Auto-generated method stub
		
	}
	
	public void addButtonsListener(OnClickListener listener){
		Iterator<Button> it = getButtonList().iterator();
		
		while (it.hasNext()) {
			Button button = (Button) it.next();
			button.setOnClickListener(listener);
		}
	}

}