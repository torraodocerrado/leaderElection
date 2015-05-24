package projects.t1;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public final class LogFile {

	private File file;
	private FileOutputStream fos;
	private double step;

	public FileOutputStream getFos() {
		return fos;
	}

	public void setFos(FileOutputStream fos) {
		this.fos = fos;
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

	public LogFile(String nameFile) {
		try {
			this.setFile(new File(nameFile));
			this.setFos(new FileOutputStream(this.file));
		} catch (FileNotFoundException e) {
			System.err.println("Erro encontrado: " + e.getMessage());
		}
	}

	public void CloseReport() {
		try {
			this.fos.close();
		} catch (IOException e) {
			System.err.println("Erro encontrado: " + e.getMessage());
		}
	}

	public void add(Object textIn) {
		try {
			String text = textIn.toString() + ";";
			this.fos.write(text.getBytes());
		} catch (IOException e) {
			System.err.println("Erro encontrado: " + e.getMessage());
		}
	}

	public void ln() {
		try {
			String text = "\n";
			this.fos.write(text.getBytes());
		} catch (IOException e) {
			System.err.println("Erro encontrado: " + e.getMessage());
		}
	}

	public void setStep(double currentTime) {
		this.step = currentTime;

	}

	public void addStep(double currentTime, Object linhe) {
		if (this.step < currentTime) {
			this.step = currentTime;
			this.add(linhe);
		}

	}
}
