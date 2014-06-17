package org.vaadin.addons.upload;

import com.vaadin.server.ClassResource;
import com.vaadin.shared.communication.PushMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.ProgressBar;
import com.vaadin.ui.UI;

/**
 * Component to show the progress of a file. It provides also cancel input from user through the delegate.
 * <br/>
 * This component actually uploads no file, but only show the progress. The progress should be updated by the actual uploader.
 * <br/>
 * Set the push mode on the UI to have this actually work. 
 * 
 * @author bogdanudrescu
 */
@SuppressWarnings("serial")
public class Progress extends CustomComponent {

	/*
	 * The label with the file name.
	 */
	private Label nameLabel = new Label();

	/*
	 * The progress bar.
	 */
	private ProgressBar progressBar = new ProgressBar();
	{
		progressBar.setImmediate(true);
	}

	/*
	 * The cancel button.
	 */
	private Button cancelButton = new Button(new ClassResource(this.getClass(), "cancel.png"));

	/*
	 * The size of the file in bytes.
	 */
	private long contentLength = -1;

	/*
	 * The composition root.
	 */
	private HorizontalLayout layout;

	/**
	 * Create an upload progress component for a file with the specified name.
	 * @param fileName		the name of the file being uploaded.
	 * @param contentLength	the size of the file in bytes.
	 */
	public Progress(String fileName, long contentLength) {

		cancelButton.addClickListener(new CancelButtonListener());

		layout = new HorizontalLayout();
		layout.addComponent(nameLabel);
		layout.addComponent(cancelButton);

		setCompositionRoot(layout);

		// Call reset for code optimization.
		reset(fileName, contentLength);
	}

	/**
	 * Reset the component.
	 * @param fileName		the new file name.
	 * @param contentLength	the new content length.
	 */
	public void reset(String fileName, long contentLength) {
		nameLabel.setValue(fileName);
		this.contentLength = contentLength;

		if (contentLength < 0) {
			progressBar.setIndeterminate(true);
		}

		layout.addComponent(progressBar, 1);
	}

	/**
	 * Sets the count of the bytes read so far.
	 * @param currentBytesCount	the current count of the bytes read.
	 */
	public void setProgressValue(final long currentBytesCount) {
		//System.out.println("setProgressValue: " + currentBytesCount + " of " + contentLength);

		if (!progressBar.isIndeterminate()) {
			progressBar.setValue((float) currentBytesCount / contentLength);

			// Update the browser client.
			UI currentUI = UI.getCurrent();
			if (currentUI.getPushConfiguration().getPushMode() == PushMode.MANUAL) {
				currentUI.push();
			}
		}
	}

	/**
	 * Inform the component that the file was successfully uploaded.
	 */
	public void setProgressDone() {
		layout.removeComponent(progressBar);
	}

	/**
	 * Inform the component that the file failed to upload.
	 */
	public void setProgressFail() {
		layout.removeComponent(progressBar);

		// TODO: shall we have something like this?
		// layout.addComponent(new Label("Failed"), 1);
	}

	/*
	 * Listen to the cancel event and notify the delegate to cancel the upload.
	 */
	private class CancelButtonListener implements ClickListener {

		/* (non-Javadoc)
		 * @see com.vaadin.ui.Button.ClickListener#buttonClick(com.vaadin.ui.Button.ClickEvent)
		 */
		@Override
		public void buttonClick(ClickEvent event) {
			// TODO: ask again if the user is sure he wants to cancel.

			delegate.cancelUpload(Progress.this);
		}

	}

	/*
	 * The upload info delegate.
	 */
	private ProgressDelegate delegate;

	/**
	 * Sets the delegate for this upload info component.
	 * @param delegate	the delegate to handle the cancel action.
	 */
	public void setDelegate(ProgressDelegate delegate) {
		this.delegate = delegate;
	}

	/**
	 * Delegate to inform that the upload should cancel.
	 */
	public static interface ProgressDelegate {

		/**
		 * Call when the user wishes to cancel the upload.
		 * @param uploadInfo	the {@link Progress} source object.
		 */
		void cancelUpload(Progress uploadInfo);

		/**
		 * Called in case the upload failed and user wants to retry.
		 * @param uploadInfo	the {@link Progress} source object.
		 */
		void retryUpload(Progress uploadInfo); // TODO: Implement the call of this method.

	}

}
