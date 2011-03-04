package rina.idd.api;

public interface IDDProvider {
	public void Allocate_Request(Object obj);
	public void addEventListener(IDDConsumer ipcProcess);
	public void removeEventListener(IDDConsumer ipcProcess);
}
