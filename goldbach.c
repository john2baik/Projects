/* THIS CODE IS MY OWN WORK. IT WAS WRITTEN WITHOUT CONSULTING A TUTOR
OR CODE WRITTEN BY OTHER STUDENTS. 
- John Baik 

This C program takes one argument, an even number greater than 2, and finds the number of primes up to the number.

Then it uses bit arrays to iterate and find the largest pair of primes that adds up to the argument as well as the number of 
prime paris that add up to the argument. 

Compile with 'gcc -o goldbach goldbach.c'

*/
#include <stdlib.h>
#include <stdio.h>

#define BITSPERSEG  (8*256*sizeof(int)) 
    
typedef struct _seg {  
    int  bits[256];      
    struct _seg  *next,*prev;        
      }seg;

void makeSegs(int);
void insertAtTail();
seg* getNewSeg();
int sieve(int);
seg* whichseg(int);
int whichint();
int whichbit();
void marknonprime(int);
int testBitIsPrime(int);
void goldbach(int);
seg *head; // save global variable head pointer
seg *tail; //global for end of the doubly linked list
seg *saved;

int main(int argc, char *argv[]){
	if (argc > 1){
		int N = atoi(argv[1]);
		if(argc >2 ){
			printf("Please provide only one numerical command line argument.\n");
			exit(0);
		}
	}
	int N = atoi(argv[1]);
	printf("Calculating odd primes up to %d...", N);
	makeSegs(N); 
	int countPrimes = sieve(N);
	printf("Found %d primes.\n", countPrimes);

	int input;
	printf("Input Even Numbers >5 below for Goldbach Tests:\n");
    
	while(scanf("%d",&input) != EOF){
		if (input > N || input % 2 != 0){
			printf("%d is larger than %d, invalid input or not an even number. Please try again", input, N);
		}
		else{
			goldbach(input);
		}
	}
	return 0;
}


void makeSegs(int N){ 
	int numOdds = (int) ((N-1)/2);
	int numSegs = numOdds / BITSPERSEG; 
	int i = 0;
	int counter = 0;
	seg *tester;
	for (i = 0; i < numSegs+1; i++){
		if (i == 0){ 
			head = getNewSeg();
			head->prev = NULL;
			tail = head;
			}
		else{
			tail->next = getNewSeg();
			tester = tail;
			tail = tail->next;
			tail->prev = tester;
		}
	}
}

seg* getNewSeg(){
	seg* newSeg = (seg*)malloc(sizeof(seg));
	int i;
	for(i = 0; i < 256; i++){
		newSeg->bits[i] = 0;
	}
	return newSeg;
}

int sieve(int N){
	int i,j;
	int countPrimes =  N/2 - 1; 
	for (i = 3; i * i <= N; i = i +2){
		if(testBitIsPrime(i) == 0){
			continue;}
	for (j = i*i; j <= N; j = j + i*2){
		if(testBitIsPrime(j) == 0){continue;}
			marknonprime(j);
			countPrimes--;
		}
	}
	return countPrimes;
}

void goldbach(int j){

	int mid = j/2;
	int lower  = 3;
	int upper = j-lower;
	seg *uSeg = whichseg(upper);
	seg *lSeg = whichseg(lower);
	int l, h;
	int count = 0;

	while(lower < mid){

		int lIndex = whichint(lower);
		int uIndex = whichint(upper);

		int lpos = whichbit(lower);
		int upos = whichbit(upper);

		if(testBitIsPrime(lower)==1 && testBitIsPrime(upper) == 1){
			count++;
			l= lower;
			h= upper;
		}
		lower += 2;
		upper-=2;
		lpos +=1;
		upos -=1;
		if (upos == -1) {
			upos = 31;
			uIndex = uIndex - 1;
		}
		if ( lpos== 32) {
			lpos = 0;
			lIndex = lIndex + 1;
		}

		if (uIndex == -1) {
			uIndex = 255;
			uSeg = uSeg->prev;
		}
		if (lIndex == 256) {
			lIndex = 0;
			lSeg = lSeg->next;
		}
		
	}	
	printf("Largest %d + %d out of %d solutions.\n", l, h, count);
}

seg* whichseg(int j){
	seg *p = head;
	int numsPerSeg = (int) (((j - 1) / 2) / BITSPERSEG); 
	int i;
	for(i = 0; i < numsPerSeg; i++)
		p = p->next;
	return p;
}

int whichint(int j){
	return (((j - 1) / 2) % BITSPERSEG) / 32;
}
int whichbit(int j){
	return (((j - 1) / 2) % BITSPERSEG) % 32; 
}
void marknonprime(int j){
	int index = whichint(j);
	int pos = whichbit(j);
	seg* p = whichseg(j);

	unsigned int flag = 1;
	flag = flag << pos; 
	p->bits[index] = (p->bits[index] | flag); 
}

int testBitIsPrime(int j){ 
	seg *p = whichseg(j); 
	int index = whichint(j);
	int pos = whichbit(j);

	unsigned int flag = 1;
	flag = (flag << pos);

	unsigned int check = (flag & p->bits[index]);
	check = (check >> pos);

	if (check == 0){
		return 1;}
	else {return 0;}
}

