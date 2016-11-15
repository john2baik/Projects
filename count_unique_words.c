//
/* This code is my own work. It was not written with a tutor or by other students - John Baik*/
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <sys/wait.h>

#include <string.h>
#include <fcntl.h>
#include <signal.h>
#include <errno.h>
#include <ctype.h>

void parsesortsupress();
void clrbuf();

/* In this program, there are 3 processors, 1. parse, 2. sort 3. supressor

*/

int main(int argc, char *argv[]){
    parsesortsupress();
    return 0;
}

void parsesortsupress(){
    
    int scnt = 1;
    int child_status;
    int success;
    int cnt = 0;
    char buf[32];
    int lower;
    
    int i = 0;
    pid_t pid;
    pid_t pid2;
   
    FILE *stdin_ptr;
    FILE *pipe_ptr;
    
    int ch;
    
    int pipefd_sort[2];
    int pipefd_supress[2];
    
    stdin_ptr = fdopen(0, "r");
    if(stdin_ptr == NULL){
        printf("The file is invalid from stdin.\n");
        return;
    }
    
    if(pipe(pipefd_sort)==-1){
        perror("pipe");
        exit(EXIT_FAILURE);
    }
    
    if(pipe(pipefd_supress)==-1){
        perror("pipe");
        exit(EXIT_FAILURE);
    }
    
    pid = fork();
    
    if(pid == -1){
        perror("fork");
        exit(EXIT_FAILURE);
    }
    
    else if(pid == 0){

        close(pipefd_sort[1]);

        dup2(pipefd_sort[0], 0);

        close(pipefd_sort[0]);

        dup2(pipefd_supress[1], 1);

        close(pipefd_supress[1]);

      	int n = execl("/bin/sort", "sort", 0);

        if(n == -1) {
            perror("execl");
            exit(EXIT_FAILURE);
        }
        exit(0);
    }
   
    else if(pid > 0){
        close(pipefd_sort[0]);
        
        pipe_ptr = fdopen(pipefd_sort[1], "w"); pipe_ptr
        
        while(1){
            if(cnt == 30){
                buf[30] = '\n';
                buf[31] = '\0';
                cnt = 0;
                fputs(buf, pipe_ptr);
            }
            ch = fgetc(stdin_ptr);
            if(feof(stdin_ptr)){
              	buf[cnt++] = '\n';
                buf[cnt++] = '\0';
                break;
		}
            else if (isalpha(ch) != 0){
                lower = tolower(ch);
                buf[cnt++] = lower;
            }
	    else if (isalpha(ch) == 0){
		if(cnt <= 2){
			clrbuf(sizeof(buf), buf);
			cnt = 0;
		}
		else{		
		buf[cnt++] = '\n';
		buf[cnt++] = '\0';
		fputs(buf, pipe_ptr);
		
		cnt = 0;
		
		}
	    }
        }
    fclose(pipe_ptr);
    }
	
	
	pid2 = fork();
	
	if(pid2 == -1){
	        perror("fork");
	        exit(EXIT_FAILURE);
    	}
	
	else if (pid2 == 0){
		close(pipefd_supress[1]);
		close(pipefd_sort[0]);
		close(pipefd_sort[1]);
		char cur_word[33];
		char prev_word[33];
		FILE *stdout_ptr;
		
		pipe_ptr = fdopen(pipefd_supress[0], "r");

		stdout_ptr = fdopen(1, "w");

		int counter = 0;

		while(1){
			if (fgets(cur_word, 32, pipe_ptr) == NULL){
				printf("Repetition: %5d, Word: %s\n", scnt, prev_word);	
				break;
			}
			if(strcmp(cur_word, prev_word) != 0 && (counter != 0)){
				printf("Repetition: %5d, Word: %s\n", scnt, prev_word);
				scnt = 1;
				counter++;
			} 
			else {
				scnt++;
				counter++;
			}
			memset(prev_word, 0, sizeof(prev_word));
			strcpy(prev_word, cur_word);
		}		
		
		fclose(pipe_ptr);
		exit(0);

	}

	close(pipefd_sort[1]);
	close(pipefd_supress[1]);

	success = wait(&child_status);
	if (success == 0) printf("child died");

	close(pipefd_sort[0]);
	close(pipefd_supress[0]);

}

void clrbuf(int size, char buffer[]){
	int i = 0;
	for(i; i<size; i++){
		buffer[i] = '\0';
	}
}




