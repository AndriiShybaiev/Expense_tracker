import { Component } from '@angular/core';

@Component({
  selector: 'app-expenses',
  templateUrl: './expenses.component.html',
  styleUrls: ['./expenses.component.css']
})
export class ExpensesComponent {
  // Temp mock data
  expenses = [
    { name: 'Coffee', amount: 3.5 },
    { name: 'Groceries', amount: 42.0 },
    { name: 'Transport', amount: 15.75 },
  ];
}
