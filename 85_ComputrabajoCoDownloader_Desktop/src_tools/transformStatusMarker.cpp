#include <stdio.h>
#include <string.h>

#define N 1024

int
main(int argc, char *argv[])
{
    char line[N];
    FILE *fd = fopen("existingList.json", "rt");
    
    while ( fgets(line, N, fd) ) {
        line[strlen(line)-1] = '\0';
        printf("db.professionalResume.update(%s, {$set: {transformStatus: 1}})\n", line);
    }
    fclose(fd);
    return 1;
}
