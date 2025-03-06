pub struct SessionCounter{
    num: i32,
}

impl SessionCounter {
    pub fn new() -> Self {
        SessionCounter { num: 0 }
    }

    pub fn get_and_increment(&mut self) -> i32 {
        let prev: i32 = self.num;

        self.num += 1;

        prev
    }
}




    // /* File counter */
    // object Counter {
    //     private val num: Var[Int] = Var(0)
    //     val increment: Observer[Unit] = num.updater((x, unit) => x + 1)

        
    //     /* Generate name: tree-{num} for file */
    //     def genName: Signal[String] = num.signal.map(numFiles => s"tree-${numFiles}")
    // }