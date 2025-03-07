/* Session ID counter */
pub struct SessionCounter{
    num: i32,
}

impl SessionCounter {
    /* Initialise new counter with 0 */
    pub fn new() -> Self {
        SessionCounter { num: 0 }
    }

    /* Returns current counter value and increments it */
    pub fn get_and_increment(&mut self) -> i32 {
        let prev: i32 = self.num;
        self.num += 1;
        prev
    }
}