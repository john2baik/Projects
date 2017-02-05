//
//
//  Created by John Baik on 10/13/16.
/*This Code is my own work. It was written without consulting a tutor or code written by other students. John Baik*/


#include <stdio.h>
#include <stdlib.h>
//for open
#include <sys/types.h>//open dir
#include <sys/stat.h>
#include <fcntl.h>
//close
#include <unistd.h>
#include <ar.h>//contains struct for ar_hdr
#include <string.h>
#include <utime.h>
#include <dirent.h>//open directory, access to directory library
#include <time.h>

#include <unistd.h>//used for getting current directory name

void quickAppend(char *, char*);//works
void clearBuf(int, char[]);//works
void TOC(char *);//works
void appendAll(char *);//works
void extract(char *, char *);//seg fault?


int main(int argc, char *argv[]){//takes key and calls the correct function from command prompt arguments

    if(argc < 3){//not enough arguments from console
        printf("Pls, I need moar arguments. format: myar key afile fname\n");
        return -1;
    }
    else{//switch statements to different methods, can handle multiple arguments
        if(strcmp(argv[1], "-q") == 0){
            int i = 3;
            while(i < argc){
            quickAppend(argv[2], argv[i]);
            i++;
            }
        }
        
        else if(strcmp(argv[1], "-x") == 0){
            int i = 3;
            while(i < argc){
            extract(argv[2], argv[i]);
            i++;
            }
        }
    
        else if(strcmp(argv[1], "-t") == 0){//only takes in one argument
            TOC(argv[2]);
        }
        else if (strcmp(argv[1], "-A") == 0){
           
            appendAll(argv[2]);
           
        }
      
    }
    return 0;
}

void quickAppend(char *arc, char *file){//given an archive and a file, append the file into the archive
    //see if arc file exists
    int arc_fd_num = open(arc, O_RDWR | O_APPEND);//O_APPEND causes each write on the file to be appended to end
    if(arc_fd_num == -1){//if archive doesnt exist, createa new Archive file
        arc_fd_num = open(arc, O_RDWR | O_CREAT, 0666);//create archive w/ 0666 flag if archive didn't exist
        write(arc_fd_num, ARMAG, SARMAG);
    }
    
    //open file and get fd of file, if nonexistent, return error
    int file_fd_num = open(file, O_RDWR);
    if(file_fd_num == -1){//if invalid file number or no file specified, exit program
        printf("Not a valid file. Please only input existing files.\n");
	return;
    }
    
    struct stat temp;//a stat struct that can hold
    fstat(file_fd_num, &temp);//copy to hold the stats of the specified file using the fd
    
    struct ar_hdr file_hdr;//create a ar_header struct for the file too
    
    //using the file from temp w/ the same fd, fill in buffers for the new file header
    //includes fname, date, user id, group id, mode, size, fmag
    //% - left allign
    sprintf(file_hdr.ar_name, "%-16s", file);//add "/" after the file name
    sprintf(file_hdr.ar_date, "%-12ld", (long)temp.st_mtime);//time of last modification
    sprintf(file_hdr.ar_uid, "%-6ld", (long) temp.st_uid);//copy user id
    sprintf(file_hdr.ar_gid, "%-6ld", (long) temp.st_gid);//copy group id
    sprintf(file_hdr.ar_mode, "%-8o", (int) temp.st_mode);//file mode in ASCII octal
    sprintf(file_hdr.ar_size, "%-10ld", (long) temp.st_size);//file size in ASCII dec
    sprintf(file_hdr.ar_fmag, "%-2s", ARFMAG);//always contains ARFMAG,  a new string
    
    //save file size or allocate 4096 bytes as default if not found
    int buffer_size;
    //4096 is the max size of bytes a file that can occupy one block
    if(temp.st_size < 4096){
        buffer_size = temp.st_size;}
    else{ buffer_size = 4096;}
    char buf[buffer_size];//create a buffer w/ the size
    
    //collect garbage
    clearBuf(buffer_size, buf);
    
    int size_of_adhdr = sizeof(struct ar_hdr);
    //writing fd in archive with file header now populated
    write(arc_fd_num, &file_hdr, size_of_adhdr);
    
    
    int read_num = 1;
  //write into archive
    while(read_num != 0){//EOF returns 0
        read_num = read(file_fd_num, buf, buffer_size);
        write(arc_fd_num, buf, read_num);
    }
    
    close(arc_fd_num);
    close(file_fd_num);
    
}

void appendAll(char *arc){//archive all files in current directory, uses quickAppend to append all files
    int exists = open(arc, O_RDWR);
    if(exists == -1){//check if archive already exists
        printf("Archive does not exist. Create an archive first.\n");
        return;
    }
    //using DIR library to access current directory
    DIR *stream;//create a pointer to a directory stream object, filled by opendir() call
    //char buffer[50];
    stream = opendir(".");//returns a pointer to an object of type DIR, '.' refers to current directory
    
    struct dirent *current_file;//a struct pointer to a file type
    struct stat file_stat;//create a struct to access the file stats
    //struct ar_hdr file;
    //current_file = readdir(".");//populate dirent struct w/ the next file
    while((current_file = readdir(stream))!= NULL){//read files from directory until no files left

        stat(current_file->d_name, &file_stat);//create a stat header for each file

        if(S_ISREG(file_stat.st_mode) && strcmp(current_file->d_name, arc) != 0){//if file is a regular file and not in archive, since strcmp = 0 means its in archive
            quickAppend(arc, current_file->d_name);//quick append to the archive
        }
        //current_file = readdir(c_directory);//go to next file in directory
    }
    closedir(stream);
 //may come back and fix the . w/ ->
}

void extract(char *arc, char *file){//extract a file from the archive
    int arc_fd = open(arc, O_RDWR);
    if(arc_fd == -1 ){
        printf("Archive cannot be found. Please create an archive first.");
        return;
    }
    
    lseek(arc_fd, SARMAG, SEEK_SET); //set fd pointer to beginning of header
    int ar_hdr_size = sizeof(struct ar_hdr);
    int correct_file_found = 0;
    char f_name[16]; //file names of other files in archive
    char cf_names[16];//current file name in the archive being inspected
    
    struct ar_hdr name;//create ar_hdr
    strcat(file, "/");//concanate '/' to end of file
    
    while(correct_file_found != 1){//look for the file in the archive by
       int result = read(arc_fd, &name, 15); //read in size of 15 bytes from the specfied file descriptor
        if(result == 0 && correct_file_found != 1){//0 is the EOF, no file has been found
            printf("Sorry, file name could not be found in the archive. Please give a valid file name.");
            return;
        }
        strncpy(cf_names, name.ar_name, 15);//copy into cf_name, the name of the current file being inspected
        sprintf(f_name, "%0-15s", file);//copy the file name into the temp buffer
        strcat(cf_names, '\0'); //null terminated string
        if(strncmp(cf_names, f_name, 15)==0){
            correct_file_found = 1;
        }
        else{
            int file_size = atoi(name.ar_size); // returns ascii to integer the file size
            if(file_size%2 == 1){//makes the file size even to stay compatable w/ x86 archecture
                file_size +=1;
            }
            lseek(arc_fd, file_size, SEEK_CUR); //move to next file header and start over
      	}
     }	// end of while loop


	int i;
	for(i = 0; i < 16;i++){//replace the concanated / with a null terminated to be string
		if(file[i] == '/') 
			file[i] == '\0';
		i++;
    }
    int new_ffd = open(file, O_RDWR | O_CREAT);//open new file w/ the same file name

    int f_size = atoi(name.ar_size);
    int f_buf_size = (f_size < 4096) ? f_size : 4096;

    char nf_buf[f_buf_size];//kinda like malloc
    clearBuf(f_buf_size, nf_buf);

//copy file to a new file
    int x = 0, total = 0, r = 0;
    while (total < f_size) {
        r = (f_buf_size > f_size - total) ? f_size - total : f_buf_size;
        x = read(arc_fd, nf_buf, x);
        total = total + r_trans;
        write(new_ffd, nf_buf, x);
    }

    // change time to when it is now stored in the archive file
    struct utimbuf time;
    int m_time = atoi(name.ar_date);
    time.actime = m_time;
    time.modtime = m_time;

    utime(file, &time);

    close(arc_fd);
    close(new_ffd);
}

void clearBuf(int size_of_buffer, char buffer[]){//clears buffers by resetting bytes to 0, garbage collector
    int i = 0;
    for(i; i < size_of_buffer; i++){
        buffer[i] = '\0';
    }
}

void TOC(char *arch){//prints out the archvie
    int fdnum = open(arch, O_RDWR); //returns a file descriptor number
    if(fdnum == -1){//invalid fd
        printf("ls  open file.\n");
        return;
    }
    lseek(fdnum, SARMAG, SEEK_SET);//skip magic number to go to the first buffer in file hdr
    int arhdr_size = sizeof(struct ar_hdr);
    struct ar_hdr temp;
    char name_buf[16];
    
    while(read(fdnum, &temp, arhdr_size) > 0){//while it is not the end of archive
        strncpy(name_buf, temp.ar_name, 15); //copy the header name into headers
	name_buf[15] = '\0';
	
        printf("%s\n", name_buf);
        //skip all the file data bytes
        int file_data_size = atoi(temp.ar_size);//convert from ascii to integer the header size
        lseek(fdnum, file_data_size, SEEK_CUR);//skip the file data and move onto next header using lseek
    }
    close(fdnum);
}
