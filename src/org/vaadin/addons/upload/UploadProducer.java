package org.vaadin.addons.upload;

import java.io.Serializable;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.vaadin.addons.upload.UploadProgress.UploadProgressListener;
import org.vaadin.addons.upload.UploadProgress.UploadStatus;

import com.vaadin.ui.Upload.Receiver;

/**
 * Produce UploadProgresss as needed.
 * 
 * @author bogdanudrescu
 */
@SuppressWarnings("serial")
public class UploadProducer implements Serializable {

	/*
	 * Listen to each upload events.
	 */
	private UploadProgressHandler handler = new UploadProgressHandler();

	/**
	 * Create the group with 1 Upload.
	 * @param listener			listener to be notified when the UploadProgress components are created.
	 */
	public UploadProducer(UploadProducerListener listener) {
		this(1, listener);
	}

	/**
	 * Create the group with few initial uploads.
	 * @param initialUploads	the number of uploads to produce when this goes online.
	 * @param listener			listener to be notified when the UploadProgress components are created.
	 */
	public UploadProducer(int initialUploads, UploadProducerListener listener) {
		// First add the listener.
		addUploadProducerListener(listener);

		// Then creates the initial upload components.
		for (int i = 0; i < initialUploads; i++) {
			produceUpload();
		}
	}

	// TODO: maybe set a maximum upload components allowed.

	/*
	 * The uploads.
	 */
	private List<UploadProgress> uploads = new LinkedList<>(); // Maybe this is faster then ArrayList in our case...

	/**
	 * Gets the number of upload components.
	 * @return	the number of upload components.
	 */
	// TODO: maybe add methods to retrieve the upload components.
	/*
	public int getUploadCount() {
		return uploads.size();
	}
	//*/

	/**
	 * Remove the specified upload progress component.
	 * @param uploadProgress	the component to remove.
	 */
	public void removeUpload(UploadProgress uploadProgress) {
		synchronized (UploadProducer.this) {

			// This doesn't need to synchronize
			if (uploads.remove(uploadProgress)) {
				fireShouldRemoveUpload(uploadProgress);
			}
		}
	}

	/**
	 * Remove all upload progresses from the specified collection.
	 * @param uploadProgresses	the collection of upload progresses to remove.
	 */
	public void removeUploads(Collection<UploadProgress> uploadProgresses) {
		for (UploadProgress uploadProgress : uploadProgresses) {
			removeUpload(uploadProgress);
		}
	}

	/*
	 * Produce a new upload.
	 */
	private void produceUpload() {
		UploadProgress upload = new UploadProgress(handler);
		addAllListenersToUpload(upload);

		uploads.add(upload);

		fireUploadProduced(upload);
	}

	/*
	 * Gets whether any upload is available.
	 */
	private boolean isAnyUploadAvailable() {
		EnumSet<UploadStatus> enumSet = EnumSet.noneOf(UploadStatus.class); // This should be slow though, but it's fancy enough.

		Iterator<UploadProgress> iterator = uploads.iterator();
		while (iterator.hasNext()) {
			enumSet.add(iterator.next().getStatus());
		}

		return enumSet.contains(UploadStatus.NONE);
	}

	/*
	 * Manage the upload events.
	 */
	class UploadProgressHandler implements UploadProgressListener {

		/* (non-Javadoc)
		 * @see com.example.utils.upload.UploadProgress.UploadProgressListener#shouldRemoveUploadComponent(com.example.utils.upload.UploadProgress)
		 */
		@Override
		public void shouldRemoveUploadProgress(UploadProgress uploadProgress) {
			synchronized (UploadProducer.this) {

				// This doesn't need to synchronize
				uploads.remove(uploadProgress);

				// uploadProgress.removeUploadListener(this); // FIXME: either this or just remove the listeners automatically from the upload component directly. Any way there will be no further events...
			}
		}

		/* (non-Javadoc)
		 * @see com.example.utils.upload.UploadProgress.UploadProgressListener#uploadStarted(com.example.utils.upload.UploadProgress)
		 */
		@Override
		public void uploadStarted(UploadProgress uploadProgress) {
			synchronized (UploadProducer.this) { // Synch on UploadProducer.this otherwise we'll end up in a deadlock with the listener calls. 

				if (!isAnyUploadAvailable()) {
					produceUpload();
				}
			}
		}

		/* (non-Javadoc)
		 * @see com.example.utils.upload.UploadProgress.UploadProgressListener#uploadFailed(com.example.utils.upload.UploadProgress)
		 */
		@Override
		public void uploadFailed(UploadProgress uploadProgress) {
		}

		/* (non-Javadoc)
		 * @see com.example.utils.upload.UploadProgress.UploadProgressListener#uploadCanceled(com.example.utils.upload.UploadProgress)
		 */
		@Override
		public void uploadCanceled(UploadProgress uploadProgress) {
		}

		/* (non-Javadoc)
		 * @see com.example.utils.upload.UploadProgress.UploadProgressListener#uploadDone(com.example.utils.upload.UploadProgress)
		 */
		@Override
		public void uploadDone(UploadProgress uploadProgress) {
		}

	}

	/*
	 * The listeners list.
	 */
	private List<UploadProducerListener> listeners = new LinkedList<>();

	/**
	 * Adds an upload listener.
	 * @param listener	the listener to add.
	 */
	public synchronized void addUploadProducerListener(UploadProducerListener listener) {
		listeners.add(listener);

		addListenerToAllUploads(listener);
	}

	/**
	 * Adds an upload listener.
	 * @param listener	the listener to add.
	 */
	public synchronized void removeUploadProducerListener(UploadProducerListener listener) {
		listeners.remove(listener);

		removeListenerFromAllUploads(listener);
	}

	/*
	 * Add the specified listener to all uploads.
	 */
	private synchronized void addListenerToAllUploads(UploadProducerListener listener) {
		for (UploadProgress upload : uploads) {
			upload.addUploadListener(listener);
		}
	}

	/*
	 * Add the specified listener to all uploads.
	 */
	private synchronized void removeListenerFromAllUploads(UploadProducerListener listener) {
		for (UploadProgress upload : uploads) {
			upload.removeUploadListener(listener);
		}
	}

	/*
	 * Add all listeners to the specified upload.
	 */
	private synchronized void addAllListenersToUpload(UploadProgress upload) {
		for (UploadProducerListener listener : listeners) {
			upload.addUploadListener(listener);
		}
	}

	/**
	 * Remove all the listeners from the specified upload.
	 * @deprecated dangerous method to remove all listeners.
	 */
	private synchronized void removeAllListenersFromUpload(UploadProgress upload) {
		for (UploadProducerListener listener : listeners) {
			upload.removeUploadListener(listener);
		}
	}

	/**
	 * Produce an upload component.
	 * @param upload	the component produced.
	 */
	protected synchronized void fireUploadProduced(UploadProgress upload) {
		for (UploadProducerListener listener : listeners) {
			listener.uploadProgressProduced(upload);
		}
	}

	/**
	 * Notify when an upload component should be removed from the UI.
	 * @param upload	the component to remove.
	 */
	protected synchronized void fireShouldRemoveUpload(UploadProgress upload) {
		for (UploadProducerListener listener : listeners) {
			listener.shouldRemoveUploadProgress(upload);
		}
	}

	/**
	 * Receive notifications when the uploads are produced and when files are uploaded.
	 */
	public interface UploadProducerListener extends UploadProgressListener {

		/**
		 * Produce an upload component.
		 * <br/>
		 * On this method, the user can add a {@link Receiver} in case it wants to receive the data through its own stream.
		 * Otherwise it can obtain the data when the {@link #uploadDone(UploadProgress)} method gets called.
		 * @param uploadProgress	the component produced.
		 */
		void uploadProgressProduced(UploadProgress uploadProgress);

	}

	/**
	 * Adapter with no body implementation for any of the methods.
	 */
	public static abstract class UploadProducerAdapter implements UploadProducerListener {

		/* (non-Javadoc)
		 * @see com.example.utils.upload.UploadProgress.UploadProgressListener#shouldRemoveUploadProgress(com.example.utils.upload.UploadProgress)
		 */
		@Override
		public void shouldRemoveUploadProgress(UploadProgress uploadProgress) {
		}

		/* (non-Javadoc)
		 * @see com.example.utils.upload.UploadProgress.UploadProgressListener#uploadStarted(com.example.utils.upload.UploadProgress)
		 */
		@Override
		public void uploadStarted(UploadProgress uploadProgress) {
		}

		/* (non-Javadoc)
		 * @see com.example.utils.upload.UploadProgress.UploadProgressListener#uploadFailed(com.example.utils.upload.UploadProgress)
		 */
		@Override
		public void uploadFailed(UploadProgress uploadProgress) {
		}

		/* (non-Javadoc)
		 * @see com.example.utils.upload.UploadProgress.UploadProgressListener#uploadCanceled(com.example.utils.upload.UploadProgress)
		 */
		@Override
		public void uploadCanceled(UploadProgress uploadProgress) {
		}

		/* (non-Javadoc)
		 * @see com.example.utils.upload.UploadProgress.UploadProgressListener#uploadDone(com.example.utils.upload.UploadProgress)
		 */
		@Override
		public void uploadDone(UploadProgress uploadProgress) {
		}

		/* (non-Javadoc)
		 * @see com.example.utils.upload.UploadProducer.UploadProducerListener#uploadProgressProduced(com.example.utils.upload.UploadProgress)
		 */
		@Override
		public void uploadProgressProduced(UploadProgress uploadProgress) {
		}

	}

}
