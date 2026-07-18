import {Injectable} from '@angular/core';
import {HttpErrorResponse} from '@angular/common/http';

@Injectable({
    providedIn: 'root',
})
export class HttpErrorService {
    formatError(err: HttpErrorResponse): string {
        console.error('format error ', err);
        return this.httpErrorFormatter(err);
    }

    private httpErrorFormatter(err: HttpErrorResponse): string {
        let errorMessage: string;
        if (err.error instanceof ErrorEvent) {
            // A client-side or network error occurred. Handle it accordingly.
            errorMessage = `An error occurred: ${err.error.message}`;
        } else {
            // The backend returned an unsuccessful response code.
            errorMessage = `Server returned code: ${err.status}, error message is: ${err.statusText}`;
        }
        return errorMessage;
    }
}
