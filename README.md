# CLR Walk

CLR Walk is a tool for walking through the .NET CLR and inspecting the state of the CLR at any given time. It is a tool for debugging and learning about the CLR.  
This project is full of junks, so just take it as a toy.

## Functionality

- [x] Walk through the CLR
- [x] Inspect the state of the CLR
- [x] Dump CLR PE file

## Support Binary type
- [x] PE
- [x] Memory dump with Virtual Machine
- [x] Memory dump with Cheat Engine
- [x] Memory dump with WinDbg
- [x] Memory dump with x64dbg
- [x] Memory dump with x32dbg
- [x] Memory dump with MiniDumpWriteDump

So it will be useful for debugging Unity3D

## Usage

```
Usage: java -jar clrwalk.jar <path>
```

## Limitations

Due to I'm so lazy to implement KMP algorithm and file chunk loading, CLR Walk can only walk through file with size less than 2GiB - 1B.
